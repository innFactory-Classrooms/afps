package vl08

import org.apache.spark.sql.SparkSession

object StructuredNetworkWordCount {
  def main(args: Array[String]): Unit = {

    // start netcat server - nc -lk 9999`
    val host = "localhost"
    val port = 9999

    val spark = SparkSession
      .builder()
      .master("local")
      .appName("StructuredNetworkWordCount")
      .getOrCreate()

    import spark.implicits._

    val lines = spark.readStream
      .format("socket")
      .option("host", host)
      .option("port", port)
      .load()

    val words = lines.as[String].flatMap(_.split(" "))

    val wordCounts = words.groupBy("value").count()

    val query = wordCounts.writeStream
      .outputMode("complete")
      .format("console")
      .start()

    query.awaitTermination()
  }
}
