package vl08.uebung

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._


object SpotifyTopHitPerCountry {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("SpotifyTopHitPerCountry")
      .master("local")
      .getOrCreate()

    import spark.implicits._

    val path = "src/main/resources/data/universal_top_spotify_songs.csv"

    val df = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(path)

    val cleanedDF = df
      .filter(
        $"country".isNotNull &&
          length($"country") === 2 &&
          $"country".rlike("^[a-zA-Z]{2}$") &&
          $"spotify_id".isNotNull &&
          $"name".isNotNull &&
          $"artists".isNotNull &&
          $"daily_rank".isNotNull &&
          $"snapshot_date".isNotNull // Keep snapshot_date for potential future year-specific analysis if needed
      )

    val scoredDF = cleanedDF
      .withColumn("score", when($"daily_rank".between(1,50), lit(51) - $"daily_rank").otherwise(0))

    val aggDF = scoredDF.groupBy($"country", $"spotify_id", $"name", $"artists")
      .agg(
        sum($"score").as("total_score"),
        count(when($"daily_rank".between(1,50), true)).alias("days_in_charts"),
        min($"daily_rank").as("best_rank"),
        avg($"daily_rank").as("avg_rank")
      )

    val rankWindow = Window.partitionBy($"country").orderBy($"total_score".desc, $"best_rank".asc, $"avg_rank".asc)
    val result = aggDF
      .withColumn("chart_rank_per_country", row_number().over(rankWindow))
      .filter($"chart_rank_per_country" <= 3)
      .select(
        $"country",
        $"chart_rank_per_country",
        $"spotify_id",
        $"name",
        split($"artists", ",\\s*").as("artists"),
        $"total_score",
        $"days_in_charts",
        $"best_rank",
        $"avg_rank",
        concat(lit("https://open.spotify.com/intl-de/track/"), $"spotify_id").alias("spotify_url")
      )
      .orderBy($"country".asc, $"chart_rank_per_country".asc)

    result.show(250, truncate = false) // Show more rows to see results for multiple countries


    spark.stop()
  }
}