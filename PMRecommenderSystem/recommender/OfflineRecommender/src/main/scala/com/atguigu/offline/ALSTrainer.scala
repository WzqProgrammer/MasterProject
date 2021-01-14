package com.atguigu.offline

import breeze.numerics.sqrt
import com.atguigu.offline.OfflineRecommender.MONGODB_RATING_COLLECTION
import org.apache.spark.SparkConf
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object ALSTrainer {

  def main(args: Array[String]): Unit = {
    //定义配置
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://192.168.206.100:27017/recommender",
      "mongo.db" -> "recommender"
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

    //读取MongoDB中的业务数据
    val ratingRDD = spark
      .read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_RATING_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[MovieRating]
      .rdd
      .map(rating => Rating(rating.uid, rating.mid, rating.score))      //转化成RDD并且去掉时间戳
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
