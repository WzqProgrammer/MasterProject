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
*歌曲ID   sid
*歌曲标签   tags
 */
case class Song(sid: Long, tags: String)
/*
*Marking数据 uid,sid,marking,timestamp
*1,31,2.5,1260759144
 */
case class Marking(uid: Long, sid: Long, marking: Double, timestamp: Int)

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
 * @param clusterName  集群名称，默认为elasticsearch
 */
case class ESConfig(httpHosts:String, transportHosts:String, index:String, clusterName:String)

object DataLoader {

  //定义路径常量
  val SONG_DATA_PATH = "D:\\CodeProjects\\MasterProject\\PMRecommenderSystem\\recommender\\DataLoader\\src\\main\\resources\\data\\songs.csv"
  val MARKING_DATA_PATH = "D:\\CodeProjects\\MasterProject\\PMRecommenderSystem\\recommender\\DataLoader\\src\\main\\resources\\data\\marking.csv"

  //mongodb中的表名
  val MONGODB_SONG_COLLECTION = "Songs"
  val MONGODB_MARKING_COLLECTION = "Marking"

  val ES_SONG_INDEX = "Songs"

  def main(args: Array[String]): Unit ={

    //定义需要使用的配置参数
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://192.168.206.100:27017/musicRecommender",
      "mongo.db" -> "musicRecommender",
      "es.httpHosts" -> "192.168.206.100:9200",
      "es.transportHosts" -> "192.168.206.100:9300",
      "es.index" -> "music_recommender",
      "es.cluster.name" -> "elasticsearch"
    )

    //创建一个sparkConf
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("DataLoader").set("spark.testing.memory","4147480000");
    //创建一个sparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate();

    //在对DataFrame和Dataset进行操作的许多操作需要这个包支持
    import spark.implicits._

    //加载数据
    val songRDD = spark.sparkContext.textFile(SONG_DATA_PATH);
    //将 songRDD转换为 DataFrame
    val songDF = songRDD.map(
      item => {
        val attr = item.split(",")
        Song(attr(0).toLong, attr(1).trim)
      }).toDF()

    val markingRDD = spark.sparkContext.textFile(MARKING_DATA_PATH);
    //将 ratingDD装换为 DataFrame
    val markingDF = markingRDD.map(
      item => {
        val attr = item.split(",")
        Marking(attr(0).toLong, attr(1).toLong, attr(2).toDouble, attr(3).toInt)
      }).toDF()


    //声明一个隐式的配置对象 mongodb
    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    //将数据保存到MongoDB中
    storeDataInMongoDB(songDF, markingDF)


    // 当前没有tag处理
    //数据预处理，把movie对应的tag信息添加进去，加一列  tag1|tag2|tag3...
//    import org.apache.spark.sql.functions._
//
//    val newTag = tagDF.groupBy($"mid")
//        .agg(concat_ws("|", collect_set($"tag"))
//        .as("tags"))
//        .select("mid", "tags")
//    //需要将处理后的Tag数据，和Movie数据融合，产生新的Movie数据
//    val movieWithTagsDF = songDF.join(newTag, Seq("mid", "mid"), "left")


    //声明一个ES配置的隐式参数
    implicit val esConfig = ESConfig(config("es.httpHosts"),
      config("es.transportHosts"),
      config("es.index"),
      config("es.cluster.name"))

    //将歌曲信息数据保存到ES
    storeDataInES(songDF)

    spark.stop()
  }

  def storeDataInMongoDB(songDF:DataFrame, ratingDF:DataFrame)
                        (implicit mongoConfig: MongoConfig): Unit ={
    //新建一个到MongoDB的连接
    val mongoClient = MongoClient(MongoClientURI(mongoConfig.uri))
    //如果MongoDB中存在对应的数据库，应该删除
    mongoClient(mongoConfig.db)(MONGODB_SONG_COLLECTION).dropCollection()
    mongoClient(mongoConfig.db)(MONGODB_MARKING_COLLECTION).dropCollection()

    //将当前数据写入到MongoDB中
    songDF
      .write
      .option("uri",mongoConfig.uri)
      .option("collection", MONGODB_SONG_COLLECTION)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    ratingDF
      .write
      .option("uri",mongoConfig.uri)
      .option("collection",MONGODB_MARKING_COLLECTION)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()


    //对数据表建立索引
    mongoClient(mongoConfig.db)(MONGODB_SONG_COLLECTION).createIndex(MongoDBObject("mid"->1))
    mongoClient(mongoConfig.db)(MONGODB_MARKING_COLLECTION).createIndex(MongoDBObject("uid"->1))
    mongoClient(mongoConfig.db)(MONGODB_MARKING_COLLECTION).createIndex(MongoDBObject("mid"->1))

    //关闭MongoDB连接
    mongoClient.close()
  }

  def storeDataInES(songDF:DataFrame)(implicit esConfig:ESConfig): Unit ={
    //新建一个配置
    val settings:Settings = Settings.builder()
      .put("cluster.name",esConfig.clusterName).build()
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
    songDF
      .write
      .option("es.nodes", esConfig.httpHosts)
      .option("es.http.timeout","100m")
      .option("es.mapping.id", "sid")
      .mode("overwrite")
      .format("org.elasticsearch.spark.sql")
      .save(esConfig.index + "/" + ES_SONG_INDEX)
  }
}
