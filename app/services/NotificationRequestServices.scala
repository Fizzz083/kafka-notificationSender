package services

import transactions.{EmailRequestTransaction, NotificationRequestTransaction}
import models.{EmailRequestDbModel, NotificationRequest, NotificationRequestDbModel}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

class NotificationRequestServices @Inject()
(emailRequestTransaction: EmailRequestTransaction,
 notificationRequestTransaction: NotificationRequestTransaction,
 kafkaProducerServices: KafkaProducerServices,
 controllerComponents: ControllerComponents)
 (implicit executionContext: ExecutionContext)
  extends AbstractController(controllerComponents)
{
  def addNewRequest(notiReqInfoJs:JsValue) =  {
    println("Calling addNewRequest of NotificationController")

    try {
      println("Before kafka queue insertion")

      val notiStr = Json.stringify(notiReqInfoJs)

      val reqDate = (Calendar.getInstance().getTime()).toString
      val newDbModel = new NotificationRequestDbModel("email", notiStr, reqDate)

      val id = notificationRequestTransaction.insert(newDbModel) // insert to notificationRequest table
      val watchDuration = Duration(100, TimeUnit.MICROSECONDS)
      val res = Await.result(id, watchDuration)
      val value = res.get

      val emailRequestModel = new EmailRequestDbModel("Pending", "null", value)
      val emailId = emailRequestTransaction.insert(emailRequestModel) // insert to email request table
      val watchDurationEmail = Duration(100, TimeUnit.MICROSECONDS)
      val resEmail = Await.result(emailId, watchDurationEmail)
      val emailIdvalue = resEmail.get

      kafkaProducerServices.insertIntoTopic(emailIdvalue)
      println("Returning from addNewRequest of NotificationController")

      Ok("Successfully added to service with notification id = " + value + " \n email id " + emailIdvalue)
    }
    catch {
      case _ => {
        println("Returning from addNewRequest of NotificationController")
        Ok("Failed")
      }
    }
  }

  def getAllNotificationRequests = Action.async { implicit request: Request[AnyContent] =>
    notificationRequestTransaction.all().map {
      case (notificationRequest) =>
        Ok(views.html.notificationRequestList(notificationRequest))
    }
  }

  def insertNotificationRequest = Action(parse.json) { implicit request =>
    val notificationRequestJs = request.body
    val requestType = (notificationRequestJs \ "requestType").get.as[String]
    val requestDetails = (notificationRequestJs \ "requestDetails").get.as[String]
    val requestDate = (notificationRequestJs \ "requestDate").get.as[String]

    val st = new NotificationRequestDbModel(requestType, requestDetails, requestDate)
    try {
      val id = notificationRequestTransaction.insert(st)
      Ok(s"$id => success st: ${requestType},\n ${requestDetails},\n ${requestDate} ")
    }
    catch {
      case e => Ok("Failed")
    }
  }

  def updateNotificationRequest = Action(parse.json) { implicit request =>
    val notificationRequestJs = request.body
    val requestId = (notificationRequestJs \ "requestId").get.as[Int]
    val requestType = (notificationRequestJs \ "requestType").get.as[String]
    val requestDetails = (notificationRequestJs \ "requestDetails").get.as[String]
    val requestDate = (notificationRequestJs \ "requestDate").get.as[String]

    val st = new NotificationRequest(requestId, requestType, requestDetails, requestDate)
    try {
      notificationRequestTransaction.update(st)
      Ok(s"success st: ${requestType},\n ${requestDetails},\n ${requestDate} ")
    }
    catch {
      case e => Ok("Failed")
    }
  }

  def deleteNotificationRequest = Action(parse.json) { implicit request =>
    val notificationRequestJs = request.body
    val id = (notificationRequestJs \ "requestId").get.as[Int]

    try {
      notificationRequestTransaction.delete(id)
      Ok(s"successfully deteted st: ${id}-${id.getClass} ")
    }
    catch {
      case e => Ok("Failed")
    }
  }
}