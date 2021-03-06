package example

import org.scalajs.dom
import org.scalajs.dom.{KeyboardEvent, document, window}

import scala.scalajs.js
import scalatags.JsDom.all._
import org.scalajs.jquery._
import com.highcharts.HighchartsUtils._
import com.highcharts.HighchartsAliases._
import com.highcharts.config.{SeriesLine, _}
import prickle._
import shared.{SummaryMeasurement, SummaryMeasurements}

import scala.scalajs.js.timers.SetTimeoutHandle

object ScalaJSExample extends js.JSApp {
  var socket: dom.WebSocket = _
  var charts: Set[String] = Set()

  def main(): Unit = {
    socket = new dom.WebSocket(wsUrl)
    socket.onmessage = ScalaJSExample.receive _


    jQuery("#menu-toggle").click({ (e: dom.MouseEvent) =>
      e.preventDefault()
      jQuery("#wrapper").toggleClass("toggled");
      jQuery("#menu-toggle").toggleClass("toggled");
      jQuery("#nosqlmark-brand").toggleClass("toggled");
    })

    // window resize
    var res = -1
    jQuery(window).resize(() => {
      if (res != -1) {
        window.clearTimeout(res)
      }
      res = window.setTimeout(()=> {
        charts.foreach(c => resizeChart(c))
      }, 500)
    })
    ready
  }

  lazy val wsUrl: String = if(dom.window.location.protocol == "https:") {
      val ws = jQuery("#monitor_container").data("ws-url").toString
      ws.splitAt(2)._1 + "s" + ws.splitAt(2)._2
    } else {
      jQuery("#monitor_container").data("ws-url").toString
    }

  def receive(e: dom.MessageEvent) = {
    val data = Unpickle[SummaryMeasurements].fromString(e.data.toString)
    val now = js.Date.now()

    data.get.measurements.foreach(measurement => {
      val htmlId = "chart_" + measurement.operation.toLowerCase

      if (!(jQuery("#" + htmlId).length > 0)) {
        val chart = div(`class`:="col-xs-12 col-md-6 col-lg-4")(
          div(id:=htmlId)()
        )
        jQuery("#charts").get(0).appendChild(chart.render)
        jQuery("#" + htmlId).highcharts(chartLatencyConfig(measurement.operation))
      }

      charts = charts + (htmlId)

      val series = jQuery("#" + htmlId).highcharts().get.series
      series(5).addPoint(SeriesAreaData(x = now, y = measurement.mean), true, series(5).data.length > 100)
      series(4).addPoint(SeriesAreaData(x = now, y = measurement.p90), true, series(4).data.length > 100)
      series(3).addPoint(SeriesAreaData(x = now, y = measurement.p95), true, series(3).data.length > 100)
      series(2).addPoint(SeriesAreaData(x = now, y = measurement.p99), true, series(2).data.length > 100)
      series(1).addPoint(SeriesAreaData(x = now, y = measurement.p999), true, series(1).data.length > 100)
      series(0).addPoint(SeriesAreaData(x = now, y = measurement.p9999), true, series(0).data.length > 100)

      resizeChart(htmlId)
    })
  }

  def unwrapJson(json: js.Object) = {
    println("Read " + json.hasOwnProperty("Read"))
    println("Insert " + json.hasOwnProperty("Insert"))
  }

  def close() = socket.close()

  def ready = {
    dom.console.log("ready")
  }

  def chartLatencyConfig(operation: String) = new HighchartsConfig {
    override val chart: Cfg[Chart] = Chart(`type` = "area",
      backgroundColor = new js.Object {
        val linearGradient = new js.Object {
          val x1 = 0
          val y1 = 0
          val x2 = 1
          val y2 = 1
        }
        val stops = js.Array(js.Array(0, "#2a2a2b"), js.Array(1, "#2a2a2b"))
      },
      plotBorderColor = js.Object("#000000"),
      style = js.Object {
        val fontFamily = "\'Unica One\', sans-serif"
      }
    )

    override val title: Cfg[Title] = Title(text = operation + " Latency", style = new js.Object {
      val color = "#E0E0E3"
      val textTransform = "uppercase"
      val fontSize = "16px"
    })

    override val xAxis: Cfg[XAxis] = XAxis(`type` = "datetime", title = XAxisTitle(text = "time", style = new js.Object {
      val color = "#FFFFFF"
      val fontSize = "15px"
    }),
      lineColor = "#FFFFFF", minorGridLineColor = "#FFFFFF", tickColor = "#FFFFFF", labels = XAxisLabels(style = new js.Object {
        val color = "#afc4d8"
      }))

    override val yAxis: Cfg[YAxis] = YAxis(title = YAxisTitle(text = "latency (ms)", style = new js.Object {
      val color = "#FFFFFF"
      val fontSize = "15px"
    }),
      lineColor = "#FFFFFF", minorGridLineColor = "#FFFFFF", tickColor = "#FFFFFF", labels = YAxisLabels(style = new js.Object {
        val color = "#afc4d8"
      }), plotLines = js.Array(YAxisPlotLines(color = "#808080")))

    override val legend: Cfg[Legend] = Legend(enabled = true, style = new js.Object {
      val color = "#FFFFFF"
      val fontSize = "15px"
    },
      itemStyle = new js.Object {
        val color = "#FFFFFF"
        val fontSize = "12px"
      })

    override val plotOptions: Cfg[PlotOptions] = PlotOptions(PlotOptionsArea(marker = PlotOptionsAreaMarker(enabled = false, symbol = "circle")))

    override val series: SeriesCfg = js.Array[AnySeries](
      SeriesArea(name = "Mean", legendIndex = 5, index = 5),
      SeriesArea(name = "90%ile", legendIndex = 4, index = 4),
      SeriesArea(name = "95%ile", legendIndex = 3, index = 3),
      SeriesArea(name = "99%ile", legendIndex = 2, index = 2),
      SeriesArea(name = "99.9%ile", legendIndex = 1, index = 1),
      SeriesArea(name = "99.99%ile", legendIndex = 0, index = 0)
    )
  }

  def resizeChart(htmlId: String): Unit = {
    val width = jQuery("#" + htmlId).width()
    val height = width / 1.2
    jQuery("#" + htmlId).highcharts().get.setSize(width, height)
  }
}
