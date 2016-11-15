package shared

case class SummaryMeasurement(operation: String, count: Int, mean: Double, stddev: Double, min: Double, max: Double, p90: Double, p95: Double, p99: Double, p999: Double, p9999: Double)

case class SummaryMeasurements(jobID: String, runtime: Double, throughput: Double, measurements: Seq[SummaryMeasurement])

object SummaryMeasurements {
  val r = scala.util.Random

  def randomMeasurements: SummaryMeasurements = {
    val r = scala.util.Random
    val min = r.nextDouble() * 2
    val mean = min + (50 + r.nextDouble() * 200)
    val max = 2000 + (r.nextDouble() * 1000)
    val p9999 = max - (10 + r.nextDouble() * 20)
    val p999 = p9999 - (100 + r.nextDouble() * 100)
    val p99 = p999 - (500 + r.nextDouble() * 500)
    val p95 = mean + (100 + r.nextDouble() * 250)
    val p90 = mean + (50 + r.nextDouble() * 200)

    val stdev = 6 * p90 + mean

    SummaryMeasurements("ID0001", 90000.0, 10000.0,
      Seq(randomMeasurement("Read", 3),
        randomMeasurement("Update", 10),
        randomMeasurement("All", 6)))
  }

  def randomMeasurement(op: String, minFactor: Int): SummaryMeasurement = {
    val min = r.nextDouble() * minFactor
    val mean = min + (50 + r.nextDouble() * 200)
    val max = 2000 + (r.nextDouble() * 1000)
    val p9999 = max - (10 + r.nextDouble() * 20)
    val p999 = p9999 - (100 + r.nextDouble() * 100)
    val p99 = p999 - (500 + r.nextDouble() * 500)
    val p95 = mean + (100 + r.nextDouble() * 250)
    val p90 = mean + (50 + r.nextDouble() * 200)
    val stdev = 6 * p90 + mean
    SummaryMeasurement(op, 10000, mean, stdev, min, max, p90, p95, p99, p999, p9999)
  }
}

