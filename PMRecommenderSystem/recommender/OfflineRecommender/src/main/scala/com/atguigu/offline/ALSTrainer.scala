package com.atguigu.offline

import breeze.numerics.sqrt
import com.atguigu.offline.OfflineRecommender.MONGODB_RATING_COLLECTION
import org.apache.spark.SparkConf
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}
import scala.collection.mutable
import org.apache.log4j._

case class MappingSongID(m_sid:Int, sid:Long)
case class MappingUserID(m_uid:Int, uid:Long)

object ALSTrainer {
  Logger.getLogger("org.apache.spark.SparkContext").setLevel(Level.WARN)

  val MONGODB_MAPPING_SONGID_COLLECTION = "MappingSongID"
  val MONGODB_MAPPING_USERID_COLLECTION = "MappingUserID"

  def main(args: Array[String]): Unit = {
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
      .set("spark.memory.useLegacyMode", "true");

    //创建一个sparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate();

    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

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
    //获取映射表
    val msid_sid_dict = getMap2SongId(mappingSongDF)
    val sid_msid_dict = getSongId2Map(mappingSongDF)
    val muid_uid_dict = getMap2UserId(mappingUserDF)
    val uid_muid_dict = getUserId2Map(mappingUserDF)

    //读取MongoDB中的业务数据
    val ratingRDD = spark
      .read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_RATING_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[SongMarking]
      .rdd
      .map(rating =>
        Rating(uid_muid_dict.getOrElse(rating.uid, -1), sid_msid_dict.getOrElse(rating.sid, -1), rating.marking))      //转化成RDD并且去掉时间戳
      .cache()

    //随机切分数据集，生成训练集合测试集
    val splits = ratingRDD.randomSplit(Array(0.8, 0.2))
    val trainingRDD = splits(0)
    val testRDD = splits(1)

    //模型参数选择，输出最优参数
    adjustALSParam(trainingRDD, testRDD)

    spark.close()
  }

  def adjustALSParam(trainRDD: RDD[Rating], testRDD: RDD[Rating]): Unit ={
    val result = for(rank <- Array(100, 200, 250); lambda <- Array(0.001, 0.01, 0.1, 1))
      yield {
        val model = ALS.train(trainRDD, rank, 5, lambda)

        //计算当前参数对应模型的RMSE， 返回Double
        val rmse = getRMSE(model, testRDD)
        (rank, lambda, rmse)
      }

    //控制台打印输出最优参数
    println(result.minBy(_._3))
  }

  def getMap2SongId(mappingSongDF:DataFrame): mutable.Map[Int, Long] ={
     mappingSongDF.select("m_sid", "sid").rdd
      .map(row=>row.getAs[Int]("m_sid")->row.getAs[Long]("sid"))
      .collectAsMap().asInstanceOf[mutable.HashMap[Int, Long]]
  }

  def getSongId2Map(mappingSongDF:DataFrame): mutable.Map[Long, Int] ={
    mappingSongDF.select("m_sid", "sid").rdd
      .map(row=>row.getAs[Long]("sid")->row.getAs[Int]("m_sid"))
      .collectAsMap().asInstanceOf[mutable.HashMap[Long, Int]]
  }

  def getMap2UserId(mappingUserDF:DataFrame): mutable.Map[Int, Long] ={
    mappingUserDF.select("m_uid", "uid").rdd
      .map(row=>row.getAs[Int]("m_uid")->row.getAs[Long]("uid"))
      .collectAsMap().asInstanceOf[mutable.HashMap[Int, Long]]
  }

  def getUserId2Map(mappingSongDF:DataFrame): mutable.Map[Long, Int] ={
    mappingSongDF.select("m_uid", "uid").rdd
      .map(row=>row.getAs[Long]("uid")->row.getAs[Int]("m_uid"))
      .collectAsMap().asInstanceOf[mutable.HashMap[Long, Int]]
  }

  def getRMSE(model: MatrixFactorizationModel, data: RDD[Rating]): Unit ={
    //计算预测评分
    val userProducts = data.map(item => (item.user, item.product))
    val predictRating = model.predict(userProducts)

    //以uid，mid作为外键，inner join实际观测值和预测值
    val observed = data.map(item => ((item.user, item.product), item.rating))
    val predict = predictRating.map(item => ((item.user, item.product), item.rating))
    //内连接得到(uid, mid), (actual, predict)
    sqrt(
      observed.join(predict).map {
        case ((uid, mid), (actual, pre)) =>
          val err = actual - pre
          err * err
      }.mean()
    )
  }
}
