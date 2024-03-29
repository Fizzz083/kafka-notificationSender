package controllers

import models.EmailRequest
import play.api.libs.json.Json
import services.{EmailRequestServices, NotificationRequestServices}

import javax.inject._
import play.api.mvc._

import scala.concurrent.ExecutionContext


class HomeController @Inject()
(notificationRequestServices: NotificationRequestServices,
 emailRequestServices: EmailRequestServices,
 controllerComponents: ControllerComponents
)
  extends  AbstractController(controllerComponents)
{
  def index() = Action { implicit request: Request[AnyContent] =>
    println("Hello from HomeIndex")
    Ok(views.html.index())
  }

  def addNewRequest = Action(parse.json) { implicit request =>
    val notiReqInfoJs = request.body
    notificationRequestServices.addNewRequest(notiReqInfoJs)
  }

//  def allRequestStatus = Action { implicit request: Request[AnyContent] =>
//    val res = emailRequestServices.getAllEmailRequests
//    res
//    Ok(views.html.emailRequestList(res))
//    //implicit val f = Json.format[EmailRequest]
//    //Ok(Json.toJson(res)).as("application/json")
//  }
  def allRequestStatus = Action.async { implicit request: Request[AnyContent] =>
    emailRequestServices.getAllEmailRequests
  }
}