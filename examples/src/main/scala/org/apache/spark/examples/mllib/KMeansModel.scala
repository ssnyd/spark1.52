package org.apache.spark.examples.mllib

import org.apache.spark.SparkConf
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.SQLContext
import org.apache.spark.mllib.linalg.Vectors
//case class 必须前面
case class CC1(ID: String, LABEL: String, RTN5: Double, FIVE_DAY_GL: Double, CLOSE: Double, RSI2: Double, RSI_CLOSE_3: Double, PERCENT_RANK_100: Double, RSI_STREAK_2: Double, CRSI: Double)

object KMeansModel {
    
  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("test").setMaster("local")
    val sc = new SparkContext(conf)
    val data = sc.textFile("data/mllib/spykmeans.csv")

    val header = data.first
   //csv乱码,删除第一行数据,使用Excel报错,没有测试其他工具
   // val rows = data.filter(l => l != header)

    // define case class


    // comma separator split

    val allSplit = data.map(line => line.split(","))

    // map parts to case class

    val allData = allSplit.map(p => CC1(p(0).toString, p(1).toString, p(2).trim.toDouble, p(3).trim.toDouble, p(4).trim.toDouble, p(5).trim.toDouble, p(6).trim.toDouble, p(7).trim.toDouble, p(8).trim.toDouble, p(9).trim.toDouble))

    val sqlContext = new SQLContext(sc)
    // convert rdd to dataframe
    import sqlContext.implicits._
    import sqlContext._

    val allDF = allData.toDF()

    // convert back to rdd and cache the data

    val rowsRDD = allDF.rdd.map(r => (r.getString(0), r.getString(1), r.getDouble(2), r.getDouble(3), r.getDouble(4), r.getDouble(5), r.getDouble(6), r.getDouble(7), r.getDouble(8), r.getDouble(9)))

    rowsRDD.cache()

    // convert data to RDD which will be passed to KMeans and cache the data. We are passing in RSI2, RSI_CLOSE_3, PERCENT_RANK_100, RSI_STREAK_2 and CRSI to KMeans. These are the attributes we want to use to assign the instance to a cluster

    val vectors = allDF.rdd.map(r => Vectors.dense(r.getDouble(5), r.getDouble(6), r.getDouble(7), r.getDouble(8), r.getDouble(9)))

    vectors.cache()

    //KMeans model with 2 clusters and 20 iterations

    val kMeansModel = KMeans.train(vectors, 2, 20)

    //Print the center of each cluster

    kMeansModel.clusterCenters.foreach(println)

    // Get the prediction from the model with the ID so we can link them back to other information

    val predictions = rowsRDD.map { r => (r._1, kMeansModel.predict(Vectors.dense(r._6, r._7, r._8, r._9, r._10))) }

    // convert the rdd to a dataframe

    val predDF = predictions.toDF("ID", "CLUSTER")
  }
}