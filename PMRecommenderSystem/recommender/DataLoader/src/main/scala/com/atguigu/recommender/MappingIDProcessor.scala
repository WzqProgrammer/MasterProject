package com.atguigu.recommender

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.log4j._

//case class Song(sid: Long, tags: String)
//
////Marking数据 uid,sid,marking,timestamp
//case class Marking(uid: Long, sid: Long, marking: Double, timestamp: Int)

//id的映射
case class MappingSongID(m_sid:Int, sid:Long)
case class MappingUserID(m_uid:Int, uid:Long)

///**
// * @param uri MongoDB连接
// * @param db  MongoDB数据库
// */
//case class MongoConfig(uri:String, db:String)

object MyTest {

  Logger.getLogger("org.apache.spark.SparkContext").setLevel(Level.WARN)
  //mongodb中的表名
  val MONGODB_SONG_COLLECTION = "Songs"
  val MONGODB_MARKING_COLLECTION = "Marking"

  val MONGODB_MAPPING_SONGID_COLLECTION = "MappingSongID"
  val MONGODB_MAPPING_USERID_COLLECTION = "MappingUserID"

  def main(args: Array[String]): Unit = {
    //定义需要使用的配置参数
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://192.168.206.100:27017/musicRecommender",
      "mongo.db" -> "musicRecommender"
    )

    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))
    //创建一个sparkConf
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("MappingIDProcessor.scala").set("spark.testing.memory","4147480000");
    //创建一个sparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate();

    //在对DataFrame和Dataset进行操作的许多操作需要这个包支持
    import spark.implicits._

    //加载数据并进行预处理
    val songTagsDF = spark.read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_SONG_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Song]
      .toDF()

    val markingDF = spark
      .read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_MARKING_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Marking]
      .toDF()

    val mappingSongIDDF = songTagsDF.select($"sid").
      rdd.zipWithUniqueId()
      .map(item=>MappingSongID(item._2.toInt,item._1.getAs[Long]("sid"))).toDF()

    val mappingUserIDDF = markingDF.select($"uid").distinct()
      .rdd.zipWithUniqueId()
      .map(item=>MappingUserID(item._2.toInt,item._1.getAs[Long]("uid"))).toDF()

    storeDFInMongoDB(mappingSongIDDF, MONGODB_MAPPING_SONGID_COLLECTION)
    storeDFInMongoDB(mappingUserIDDF, MONGODB_MAPPING_USERID_COLLECTION)

    spark.stop()
  }

  def storeDFInMongoDB(df:DataFrame, collection_name:String)(implicit mongoConfig: MongoConfig): Unit ={
    df.write
      .option("uri", mongoConfig.uri)
      .option("collection", collection_name)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()
  }

}
