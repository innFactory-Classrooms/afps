package vl08.uebung

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

object SpotifyTop50Germany2024 {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("SpotifyTop50Germany2024")
      .master("local")
      .getOrCreate()

    import spark.implicits._

    // Download: https://www.kaggle.com/datasets/asaniczka/top-spotify-songs-in-73-countries-daily-updated/data
    val path = "src/main/resources/data/universal_top_spotify_songs.csv"

    val df = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(path)

    val germany2024DF = df
      .filter($"country" === "DE" && $"snapshot_date".startsWith("2024") &&
        $"spotify_id".isNotNull && $"name".isNotNull && $"daily_rank".isNotNull
      )

    val scoredDF = germany2024DF
      .withColumn("score", when($"daily_rank".between(1,50), lit(51) - $"daily_rank").otherwise(0))

    val aggDF = scoredDF.groupBy($"spotify_id", $"name", $"artists")
      .agg(
        sum($"score").as("total_score"),
        count(when($"daily_rank".between(1,50), true)).alias("days_in_charts"),
        min($"daily_rank").as("best_rank"),
        avg($"daily_rank").as("avg_rank")
      )

    val rankWindow = Window.orderBy($"total_score".desc, $"best_rank".asc, $"avg_rank".asc)
    val result = aggDF
      .withColumn("chart_rank_2024", row_number().over(rankWindow))
      .filter($"chart_rank_2024" <= 50)
      .select(
        $"chart_rank_2024",
        $"spotify_id",
        $"name",
        split($"artists", ",\\s*").as("artists"),
        $"total_score",
        $"days_in_charts",
        $"best_rank",
        $"avg_rank",
        concat(lit("https://open.spotify.com/intl-de/track/"), $"spotify_id", lit(" ")).alias("spotify_url")
      )
      .orderBy($"chart_rank_2024".asc)

    result.show(50, truncate = false)

    println("Drücke Enter, um die Anwendung zu beenden...")
    scala.io.StdIn.readLine()
    spark.stop()
  }
}