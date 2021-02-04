package com.atguigu.recommender

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.log4j._
import scala.collection.mutable

//id的映射
//case class MappingSongID(m_sid:Int, sid:Long)
//case class MappingUserID(m_uid:Int, uid:Long)
//
///**
// * @param uri MongoDB连接
// * @param db  MongoDB数据库
// */
//case class MongoConfig(uri:String, db:String)

object TestMapping{

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
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("TestMapping").set("spark.testing.memory","4147480000");
    //创建一个sparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate();

    //在对DataFrame和Dataset进行操作的许多操作需要这个包支持
    import spark.implicits._

    //加载数据并进行预处理
    val mappingSongDF = spark.read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_MAPPING_SONGID_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[MappingSongID]
      .toDF()

    val mappingUserDF = spark
      .read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_MAPPING_USERID_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[MappingUserID]
      .toDF()

   val msid_sid_dict =  getMap2SongId(mappingSongDF)
    print(msid_sid_dict.get(1))

    val sid_msid_dict =  mappingSongDF.select("m_sid", "sid").rdd
      .map(row=>row.getAs[Long]("sid")->row.getAs[Int]("m_sid"))
      .collectAsMap()

    val muid_uid_dict =  mappingUserDF.select("m_uid", "uid").rdd
      .map(row=>row.getAs[Int]("m_uid")->row.getAs[Long]("uid"))
      .collectAsMap()

    val uid_muid_dict =  mappingUserDF.select("m_uid", "uid").rdd
      .map(row=>row.getAs[Long]("uid")->row.getAs[Int]("m_uid"))
      .collectAsMap()

    spark.stop()
  }

  def getMap2SongId(mappingSongDF:DataFrame): mutable.Map[Int, Long] ={
    mappingSongDF.select("m_sid", "sid").rdd
      .map(row=>row.getAs[Int]("m_sid")->row.getAs[Long]("sid"))
      .collectAsMap().asInstanceOf[mutable.HashMap[Int, Long]]
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
