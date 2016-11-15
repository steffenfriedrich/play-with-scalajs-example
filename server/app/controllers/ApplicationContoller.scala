package controllers

import javax.inject.Singleton

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Scheduler}
import akka.stream.Materializer
import com.google.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import shared.{SharedMessages, SummaryMeasurement, SummaryMeasurements}
import play.api.libs.streams.ActorFlow
import prickle._

import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class ApplicationContoller @Inject()(system: ActorSystem, materializer: Materializer) extends Controller {
  val User = "user"
  implicit val implicitMaterializer: Materializer = materializer
  implicit val implicitActorSystem: ActorSystem = system

  def index = Action { implicit request =>
    request.session.get(User).map { user =>
      Ok(views.html.index(user))
    }.getOrElse(Redirect(routes.ApplicationContoller.index())).withSession(request.session + (User -> "Bla"))
  }

  /**
    * Handles the websocket
    */
  def websocket = WebSocket.acceptOrResult[JsValue, JsValue] { implicit request =>
    Future.successful(request.session.get(User) match {
      case None => Left(Forbidden)
      case Some(_) =>
        Right(ActorFlow.actorRef(MyActor.props))
    })
  }

  object MyActor {
    def props(out: ActorRef) = Props(new MyActor(out))
  }

  class MyActor(out: ActorRef) extends Actor {

    import play.api.libs.json._
    import system.dispatcher

    system.scheduler.schedule(0 seconds, 1 seconds) {
      out ! Json.parse(getData)
    }

    def receive = {
      case msg: JsValue => {
        println(msg)
        out ! msg
      }
    }
  }


  def getData: String = {
    val r = scala.util.Random
    val min = r.nextDouble() * 2
    val mean = min +  (50 + r.nextDouble() * 200)
    val max = 2000 + (r.nextDouble() * 1000)
    val p9999 = max - (10 + r.nextDouble() * 20)
    val p999 = p9999  - (100 + r.nextDouble() * 100)
    val p99 = p999 - (500 + r.nextDouble() * 500)
    val p95 = mean + (100 + r.nextDouble() * 250)
    val p90 = mean + (50 + r.nextDouble() * 200)

    val stdev = 6 * p90 + mean


    Pickle.intoString(SummaryMeasurements("ID0001", 90000.0, 10000.0,
      Seq(SummaryMeasurement("Read", 10000, mean, stdev, min, max, p90, p95, p99, p999, p9999),
        SummaryMeasurement("Update", 10000, (min +  (35 + r.nextDouble() * 190)),  6.4 * p90 + mean, min, max, p90, p95, p99, p999, p9999),
        SummaryMeasurement("All", 10000, (min +  (32 + r.nextDouble() * 195)), stdev, min, max, p90, p95, p99, p999, p9999)
      )))
  }
}
