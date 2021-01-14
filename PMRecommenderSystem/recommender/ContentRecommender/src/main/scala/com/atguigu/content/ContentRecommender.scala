package com.atguigu.content

import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.jblas.DoubleMatrix

//需要的数据源是电影内容信息
case class Movie(mid: Int, name: String, descri: String, timelong: String, issue: String,
                 shoot: String, language: String, genres: String, actors: String, directors: String)

case class MongoConfig(uri:String, db:String)

//标准推荐对象
case class Recommendation(mid: Int, score: Double)

//电影相似度（电影推荐）
case class MovieRecs(mid: Int, recs: Seq[Recommendation])

object ContentRecommender {

  //定义表名和常量名
  val MONGODB_MOVIE_COLLECTION = "Movie"

  val CONTENT_MOVIE_RECS = "ContentMovieRecs"

  def main(args: Array[String]): Unit = {
    //定义配置
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://192.168.206.100:27017/recommender",
      "mongo.db" -> "recommender"
    )

    //创建一个sparkConf
    val sparkConf = new SparkConf().setMaster(config("spark.cores"))
      .setAppName("ContentRecommender")
      .set("spark.executor.memory","6g")
      .set("spark.driver.memory","4g")
      .set("spark.memory.useLegacyMode", "false")

    //创建一个sparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate();

    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    //在对DataFrame和Dataset进行操作的许多操作需要这个包支持
    import spark.implicits._

    //加载数据并进行预处理
    val movieTagsDF = spark.read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_MOVIE_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Movie]
      .map(
        //提取mid、name、genres三项作为原始内容特征，分词器默认按照空格做分词
        x => (x.mid, x.name, x.genres.map(c => if(c=='|') ' ' else c))
      )
      .toDF("mid", "name", "genres")
      .cache()

    //核心部分：用TF-IDF从内容信息中提取电影特征向量
    //创建一个分词器，默认按空格分词
    val tokenizer = new Tokenizer().setInputCol("genres").setOutputCol("words")

    //用分词器对原始数据进行转换，生成新的一列words
    val wordsData = tokenizer.transform(movieTagsDF)

    //引入Hashing工具，可以将一个词语序列转化成对应的词频
    val hashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(50)
    val featurizedData = hashingTF.transform(wordsData)

    //引入IDF工具，可以得到idf模型
    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    //训练idf模型，得到每个词的逆文档频率
    val idfModel = idf.fit(featurizedData)
    //用模型对原数据进行处理，得到文档中每个词的tf-idf，作为新的特征向量
    val rescaledData = idfModel.transform(featurizedData)

    //表格信息显示
//    rescaledData.show(truncate = false)

    //从内容信息中提取电影特征向量
    val movieFeatures = rescaledData.map(
      raw => (raw.getAs[Int]("mid"), raw.getAs[SparseVector]("features").toArray)
    )
        .rdd
        .map(
          x => (x._1, new DoubleMatrix(x._2))
        )

    //对所有电影两两计算他们的相似度，先做笛卡尔积
    val movieRecs = movieFeatures.cartesian(movieFeatures)
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
        case (mid, items) => MovieRecs(mid, items.toList.sortWith(_._2 > _._2).map(x=>Recommendation(x._1, x._2)))
      }
      .toDF()

    storeDFInMongoDB(movieRecs, CONTENT_MOVIE_RECS)

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
