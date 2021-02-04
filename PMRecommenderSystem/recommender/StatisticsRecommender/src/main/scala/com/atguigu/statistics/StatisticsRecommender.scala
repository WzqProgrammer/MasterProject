package com.atguigu.statistics

import java.sql.Date
import java.text.SimpleDateFormat

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

case class Song(sid: Long, tags: String)

case class Marking(uid: Long, sid: Long, marking: Double, timestamp: Int)

case class MongoConfig(uri:String, db:String)

//定义一个基准评分对象
case class Recommendation(sid:Long, score:Double)

//定义一个音乐情感类别推荐对象
case class GenresRecommendation(genres:String, recs:Seq[Recommendation])

object StatisticsRecommender {

  //mongodb中的表名
  val MONGODB_SONG_COLLECTION = "Songs"
  val MONGODB_MARKING_COLLECTION = "Marking"

  //统计的表的名称，需要写入的数据
  val MARK_COUNT_SONGS = "MarkCountSongs"
  val MARK_COUNT_RECENTLY_SONGS = "MarkCountRecentlySongs"
  val AVERAGE_SONGS = "AverageSongs"
  val MAXIMUM_SONGS = "MaximumSongs"
  val TAGS_TOP_SONGS = "TagsTopSongs"

  def main(args: Array[String]): Unit ={

    //定义需要使用的配置参数
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://192.168.206.100:27017/musicRecommender",
      "mongo.db" -> "musicRecommender"
    )

    //创建一个sparkConf
    val sparkConf = new SparkConf().setMaster(config("spark.cores"))
      .setAppName("StatisticsRecommender")
      .set("spark.testing.memory","4147480000")
      .set("spark.memory.useLegacyMode", "true")

    //创建一个sparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate();

    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    //在对DataFrame和Dataset进行操作的许多操作需要这个包支持
    import spark.implicits._

    //将数据加载进来
    val markingDF = spark
      .read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_MARKING_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Marking]
      .toDF()

    val songDF = spark
      .read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_SONG_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Song]
      .toDF()

    //创建名为markings的临时表(视图)
    markingDF.createOrReplaceTempView("markings")

    //不同的统计推荐结果
    //1. 历史热门统计，历史评分数据最多，sid，count
    val markCountSongDF = spark.sql("select sid, count(sid) as count from markings group by sid")
    //将结果写入对应的mongodb表中
    storeDFInMongoDB(markCountSongDF, MARK_COUNT_SONGS)

    //2. 近期热门统计，按照“yyyyMM”格式选取最近的评分数据，统计评分个数
    //创建一个日期格式化工具
    val simpleDataFormat = new SimpleDateFormat("yyyyMM")
    //注册udf，把时间戳转换成年月格式
    spark.udf.register("changeDate", (x:Int)=>simpleDataFormat.format(new Date(x * 1000L)).toInt)

    //对原始数据进行预处理，去掉uid
    val ratingOfYearMonth = spark.sql("select sid, changeDate(timestamp) as yearmonth from markings")
    ratingOfYearMonth.createOrReplaceTempView("ratingOfMonth")

    //从ratingOfMonth中查找电影在各个月份的评分，mid，count，yearmonth
    val markCountRecentlySongsDF = spark.sql("select sid, count(sid) as count, yearmonth from ratingOfMonth group by yearmonth, sid order by yearmonth desc, count desc")
    //存入mongodb
    storeDFInMongoDB(markCountRecentlySongsDF, MARK_COUNT_RECENTLY_SONGS)

    //3.1 音乐评分统计，平均评分，sid，avg
    val averageSongsDF = spark.sql("select sid, avg(marking) as avg from markings group by sid")
    //存入mongodb
    storeDFInMongoDB(averageSongsDF, AVERAGE_SONGS)

    //3.2 音乐评分统计，最大评分，sid，max
    val maxSongsDF = spark.sql("select sid, max(marking) as max from markings group by sid")
    //存入mongodb
    storeDFInMongoDB(maxSongsDF, MAXIMUM_SONGS)

    //4. 各类别电影的Top统计
    //定义情感相关类别类别
    val emotionGenres = List("治愈","安静","放松","舒缓","欢快",
      "清新","轻松","伤感","温柔","轻快","舒心","诡异","恐怖","黑暗","丧","孤独",
      "悲伤","唯美","感动","催泪","宁静","惊悚","致郁","愉快")

    //TODO 当有真正用户评分时使用平均评分
    //将平均评分加入movie表里，加一列，inner join
//    val songWithScore = songDF.join(averageSongsDF, Seq("sid"))

    //将最大评分加入song表里，由于当前评分与用户评分有差异，先试用最大值填充，加一列，inner join
    val songWithScore = songDF.join(maxSongsDF, Seq("sid"))
    //为做笛卡尔积，将emotionGenres转成RDD
    val genresRDD = spark.sparkContext.makeRDD(emotionGenres)
    //计算类别Top50，首先对类别和音乐做笛卡尔积
    val genresTopSongsDF = genresRDD.cartesian(songWithScore.rdd)
        .filter{
          //条件过滤，找出song的字段tags值包含当前类别的那些
          case (genre, songRow)=>songRow.getAs[String]("tags").contains(genre)
        }
        .map{
          case (genre, songRow)=>(genre, (songRow.getAs[Long]("sid"), songRow.getAs[Double]("max")))
        }
        .groupByKey()
        .map{
          case (genre, items)=>GenresRecommendation(genre, items.toList.sortWith(_._2 > _._2).take(50).map(item=>Recommendation(item._1, item._2)))
        }
        .toDF()

    storeDFInMongoDB(genresTopSongsDF, TAGS_TOP_SONGS)

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
