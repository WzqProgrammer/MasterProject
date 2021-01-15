package com.atguigu.recommender

import java.net.InetAddress

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.{InetSocketTransportAddress, TransportAddress}
import org.elasticsearch.transport.client.PreBuiltTransportClient


/*
*电影ID   mid
* 电影名称   name
* 详情描述   descri
* 时长   timelong
* 发行时间   issue
* 拍摄时间   shoot
* 语言    language
* 类型    genres
* 演员表   actors
* 导演   directors
 */

case class Movie(mid: Int, name: String, descri: String, timelong: String, issue: String,
                 shoot: String, language: String, genres: String, actors: String, directors: String)
/*
*Rating数据集
*1,31,2.5,1260759144
 */
case class Rating(uid: Int, mid: Int, score: Double, timestamp: Int)
/*
*Rating数据集
*15,1955,dentist,1193435061
 */
case class Tag(uid: Int, mid: Int, tag: String, timestamp: Int)

//把MongoDB和ES的配置封装成样例类
/**
 * @param uri MongoDB连接
 * @param db  MongoDB数据库
 */
case class MongoConfig(uri:String, db:String)

/**
 *
 * @param httpHosts http主机列表，逗号分隔
 * @param transportHosts   transport主机列表
 * @param index     需要操作的索引
 * @param clustername  集群名称，默认为elasticsearch
 */
case class ESConfig(httpHosts:String, transportHosts:String, index:String, clustername:String)

object DataLoader {

  //定义路径常量
  val MOVIE_DATA_PATH = "D:\\CodeProjects\\GitProjects\\MyRecommenderSystem\\recommender\\DataLoader\\src\\main\\resources\\movies.csv"
  val RATING_DATA_PATH = "D:\\CodeProjects\\GitProjects\\MyRecommenderSystem\\recommender\\DataLoader\\src\\main\\resources\\ratings.csv"
  val TAG_DATA_PATH = "D:\\CodeProjects\\GitProjects\\MyRecommenderSystem\\recommender\\DataLoader\\src\\main\\resources\\tags.csv"

  //mongodb中的表名
  val MONGODB_MOVIE_COLLECTION = "Movie"
  val MONGODB_RATING_COLLECTION = "Rating"
  val MONGODB_TAG_COLLECTION = "Tag"

  val ES_MOVIE_INDEX = "Movie"

  def main(args: Array[String]): Unit ={

    //定义需要使用的配置参数
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://192.168.206.100:27017/recommender",
      "mongo.db" -> "recommender",
      "es.httpHosts" -> "192.168.206.100:9200",
      "es.transportHosts" -> "192.168.206.100:9300",
      "es.index" -> "recommender",
      "es.cluster.name" -> "elasticsearch"
    )

    //创建一个sparkConf
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("DataLoader").set("spark.testing.memory","4147480000");
    //创建一个sparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate();

    //在对DataFrame和Dataset进行操作的许多操作需要这个包支持
    import spark.implicits._

    //加载数据
    val movieRDD = spark.sparkContext.textFile(MOVIE_DATA_PATH);
    //将 movieRDD装换为 DataFrame
    val movieDF = movieRDD.map(
      item => {
        val attr = item.split("\\^")
        Movie(attr(0).toInt, attr(1).trim, attr(2).trim, attr(3).trim, attr(4).trim, attr(5).trim, attr(6).trim, attr(7).trim, attr(8).trim, attr(9).trim)
      }).toDF()

    val ratingRDD = spark.sparkContext.textFile(RATING_DATA_PATH);
    //将 ratingDD装换为 DataFrame
    val ratingDF = ratingRDD.map(
      item => {
        val attr = item.split(",")
        Rating(attr(0).toInt, attr(1).toInt, attr(2).toDouble, attr(3).toInt)
      }).toDF()

    val tagRDD = spark.sparkContext.textFile(TAG_DATA_PATH)
    //将 tagRDD装换为 DataFrame
    val tagDF = tagRDD.map(item => {
      val attr = item.split(",")
      Tag(attr(0).toInt,attr(1).toInt,attr(2).trim,attr(3).toInt)
    }).toDF()

    //声明一个隐式的配置对象 mongodb
    implicit val mongoConfig = MongoConfig(config.get("mongo.uri").get, config.get("mongo.db").get)

    //将数据保存到MongoDB中
    storeDataInMongoDB(movieDF, ratingDF, tagDF)

    //数据预处理，把movie对应的tag信息添加进去，加一列  tag1|tag2|tag3...
    import org.apache.spark.sql.functions._

    val newTag = tagDF.groupBy($"mid")
        .agg(concat_ws("|", collect_set($"tag"))
        .as("tags"))
        .select("mid", "tags")
    //需要将处理后的Tag数据，和Movie数据融合，产生新的Movie数据
    val movieWithTagsDF = movieDF.join(newTag, Seq("mid", "mid"), "left")

    //声明一个ES配置的隐式参数
    implicit val esConfig = ESConfig(config("es.httpHosts"),
      config("es.transportHosts"),
      config("es.index"),
      config("es.cluster.name"))

    //保存数据到ES
    storeDataInES(movieWithTagsDF)

    spark.stop()
  }

  def storeDataInMongoDB(movieDF:DataFrame, ratingDF:DataFrame, tagDF:DataFrame)
                        (implicit mongoConfig: MongoConfig): Unit ={
    //新建一个到MongoDB的连接
    val mongoClient = MongoClient(MongoClientURI(mongoConfig.uri))
    //如果MongoDB中存在对应的数据库，应该删除
    mongoClient(mongoConfig.db)(MONGODB_MOVIE_COLLECTION).dropCollection()
    mongoClient(mongoConfig.db)(MONGODB_RATING_COLLECTION).dropCollection()
    mongoClient(mongoConfig.db)(MONGODB_TAG_COLLECTION).dropCollection()

    //将当前数据写入到MongoDB中
    movieDF
      .write
      .option("uri",mongoConfig.uri)
      .option("collection", MONGODB_MOVIE_COLLECTION)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    ratingDF
      .write
      .option("uri",mongoConfig.uri)
      .option("collection",MONGODB_RATING_COLLECTION)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    tagDF
      .write
      .option("uri",mongoConfig.uri)
      .option("collection",MONGODB_TAG_COLLECTION)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    //对数据表建立索引
    mongoClient(mongoConfig.db)(MONGODB_MOVIE_COLLECTION).createIndex(MongoDBObject("mid"->1))
    mongoClient(mongoConfig.db)(MONGODB_RATING_COLLECTION).createIndex(MongoDBObject("uid"->1))
    mongoClient(mongoConfig.db)(MONGODB_RATING_COLLECTION).createIndex(MongoDBObject("mid"->1))
    mongoClient(mongoConfig.db)(MONGODB_TAG_COLLECTION).createIndex(MongoDBObject("uid"->1))
    mongoClient(mongoConfig.db)(MONGODB_TAG_COLLECTION).createIndex(MongoDBObject("mid"->1))

    //关闭MongoDB连接
    mongoClient.close()
  }

  def storeDataInES(movieDF:DataFrame)(implicit esConfig:ESConfig): Unit ={
    //新建一个配置
    val settings:Settings = Settings.builder()
      .put("cluster.name",esConfig.clustername).build()
    //新建一个ES的客户端
    val esClient = new PreBuiltTransportClient(settings)

    //需要将TransportHosts添加到esClient中
    val REGEX_HOST_PORT = "(.+):(\\d+)".r
    esConfig.transportHosts.split(",").foreach{
      case REGEX_HOST_PORT(host:String, port:String)=>{
        esClient.addTransportAddress(new
            InetSocketTransportAddress(InetAddress.getByName(host),port.toInt))
      }
    }

    //清除ES中遗留的数据
    if(esClient.admin().indices().exists(new
    IndicesExistsRequest(esConfig.index)).actionGet().isExists){
      esClient.admin().indices().delete(new DeleteIndexRequest(esConfig.index))
    }
    esClient.admin().indices().create(new CreateIndexRequest(esConfig.index))

    //将数据写入到ES中
    movieDF
      .write
      .option("es.nodes", esConfig.httpHosts)
      .option("es.http.timeout","100m")
      .option("es.mapping.id", "mid")
      .mode("overwrite")
      .format("org.elasticsearch.spark.sql")
      .save(esConfig.index + "/" + ES_MOVIE_INDEX)
  }
}
