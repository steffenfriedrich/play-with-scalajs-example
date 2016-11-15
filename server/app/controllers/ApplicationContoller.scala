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
      out ! Json.parse(Pickle.intoString(SummaryMeasurements.randomMeasurements))
    }

    def receive = {
      case msg: JsValue => {
        println(msg)
        out ! msg
      }
    }
  }



}
