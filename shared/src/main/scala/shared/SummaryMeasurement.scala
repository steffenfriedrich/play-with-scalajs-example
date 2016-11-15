package shared;

case class SummaryMeasurement(operation: String, count: Int, mean: Double, stddev: Double, min: Double, max: Double, p90:Double, p95:Double, p99:Double, p999: Double, p9999: Double)
case class SummaryMeasurements(jobID: String, runtime: Double, throughput: Double, measurements: Seq[SummaryMeasurement])