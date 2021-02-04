package com.atguigu.offline

import com.atguigu.offline.ALSTrainer.{MONGODB_MAPPING_SONGID_COLLECTION, MONGODB_MAPPING_USERID_COLLECTION, getMap2SongId, getMap2UserId, getSongId2Map, getUserId2Map}
import org.apache.spark.SparkConf
import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.jblas.DoubleMatrix

case class Song(sid: Long, tags: String)

case class SongMarking(uid: Long, sid: Long, marking: Double, timestamp: Int)

case class MongoConfig(uri:String, db:String)

//标准推荐对象
case class Recommendation(sid: Int, score: Double)

//用户推荐
case class UserRecs(uid: Int, recs: Seq[Recommendation])

//音乐相似度（音乐推荐）
case class MovieRecs(sid: Int, recs: Seq[Recommendation])

object OfflineRecommender {

  //定义相关常量
  val MONGODB_SONG_COLLECTION = "Song"
  val MONGODB_RATING_COLLECTION = "Marking"

  //推荐表的名称
  val USER_RECS = "UserRecs"
  val SONG_RECS = "SongRecs"

  //为用户最大推荐音乐数量
  val USER_MAX_RECOMMENDATION = 20

  def main(args: Array[String]): Unit ={

    //定义配置
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://192.168.206.100:27017/musicRecommender",
      "mongo.db" -> "musicRecommender"
    )

    //创建一个sparkConf
    val sparkConf = new SparkConf().setMaster(config("spark.cores"))
      .setAppName("OfflineRecommender")
      .set("spark.testing.memory","4147480000")
      .set("spark.memory.useLegacyMode", "true")

    //创建一个sparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate();

    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    //在对DataFrame和Dataset进行操作的许多操作需要这个包支持
    import spark.implicits._

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
    //获取映射表
    val msid_sid_dict = getMap2SongId(mappingSongDF)
    val sid_msid_dict = getSongId2Map(mappingSongDF)
    val muid_uid_dict = getMap2UserId(mappingUserDF)
    val uid_muid_dict = getUserId2Map(mappingUserDF)

    val ratingRDD = spark
      .read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_RATING_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[SongMarking]
      .rdd
      .map(rating => (uid_muid_dict.getOrElse(rating.uid, -1), sid_msid_dict.getOrElse(rating.sid, -1), rating.marking))      //转化成RDD并且去掉时间戳
      .cache()

    //从rating数据中提取所有的uid和sid，并去重
    val userRDD = ratingRDD.map(_._1).distinct()
    val songRDD = ratingRDD.map(_._2).distinct()

    //训练隐语义模型
    val trainData = ratingRDD.map(x => Rating(x._1, x._2, x._3))

    val (rank, iterations, lambda) = (100, 5, 0.001)
    val model = ALS.train(trainData, rank, iterations, lambda)

    //基于用户和电影的隐特征，计算预测评分，得到用户推荐列表
    //计算用户推荐矩阵
    val userMovies = userRDD.cartesian(songRDD)

    //调用model的predict方法预测评分
    val preRatings = model.predict(userMovies)

    val userRecs = preRatings
        .filter(_.rating > 0)              //过滤出评分大于0的项
        .map(rating => (rating.user, (rating.product, rating.rating)))
        .groupByKey()
        .map{
          case (uid, recs) => UserRecs(uid, recs.toList.sortWith(_._2 > _._2).take(USER_MAX_RECOMMENDATION)
          .map(x=>Recommendation(x._1, x._2)))
        }.toDF()

    //将表存放到MongoDB
    storeDFInMongoDB(userRecs, USER_RECS)

    //基于电影隐特征，计算相似度矩阵，得到电影的相似度列表（为实时推荐准备）
    val songsFeatures = model.productFeatures.map{
      case (sid, features) => (sid, new DoubleMatrix(features))
    }

    //对所有电影两两计算他们的相似度，先做笛卡尔积
    val songRecs = songsFeatures.cartesian(songsFeatures)
        .filter{
          //将自己与自己的匹配过滤
          case (a, b) => a._1 != b._1
        }
        .map{
          case (a, b) => {
            val simScore = this.consinSim(a._2, b._2)
            (a._1, (b._1, simScore))
          }
        }
        .filter(_._2._2 > 0.6)      //过滤出相似度大于0.6的
        .groupByKey()
        .map{
          case (sid, items) => MovieRecs(sid, items.toList.sortWith(_._2 > _._2).map(x=>Recommendation(x._1, x._2)))
        }
        .toDF()

    storeDFInMongoDB(songRecs, SONG_RECS)

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

  //相似度矩阵计算
  def consinSim(movie1: DoubleMatrix, movie2: DoubleMatrix): Double={
    movie1.dot(movie2) / (movie1.norm2() * movie2.norm2())
  }
}
