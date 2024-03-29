package services

import transactions.EmailRequestTransaction
import models.{EmailRequest, EmailRequestDbModel}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json._
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.ExecutionContext


class EmailRequestServices @Inject()
(emailRequestTransaction: EmailRequestTransaction,
 controllerComponents: ControllerComponents)
(implicit executionContext: ExecutionContext )
  extends AbstractController(controllerComponents)
{

  def getAllEmailRequests = {
    emailRequestTransaction.all().map {
      case (emailRequest) =>
        implicit val f = Json.format[EmailRequest]

        //Ok(views.html.emailRequestList(emailRequest))
        Ok(Json.toJson(emailRequest)).as("application/json")
    }
  }

  def insertEmailRequest = Action(parse.json) { implicit request =>
    val emailRequestJs = request.body
    val status = (emailRequestJs \ "status").get.as[String]
    val responseId = (emailRequestJs \ "responseId").get.as[String]
    val requestId = (emailRequestJs \ "requestId").get.as[Int]

    val st = new EmailRequestDbModel(status, responseId, requestId)
    try {
      emailRequestTransaction.insert(st)
      Ok(s"success st: ${status},\n ${responseId},\n ${requestId} ")
    }
    catch {
      case e => Ok("failed")
    }
  }


  def updateEmailRequest = Action(parse.json) { implicit request =>
    val emailRequestJs = request.body
    val emailId = (emailRequestJs \ "emailId").get.as[Int]
    val status = (emailRequestJs \ "status").get.as[String]
    val responseId = (emailRequestJs \ "responseId").get.as[String]
    val requestId = (emailRequestJs \ "requestId").get.as[Int]

    val st = new EmailRequest(emailId, status, responseId, requestId)
    try {
      emailRequestTransaction.update(st)
      Ok(s"success st: ${emailId},\n ${status},\n ${responseId},\n ${requestId} ")
    }
    catch {
      case e => Ok("failed")
    }
  }


  def deleteEmailRequest = Action(parse.json) { implicit request =>
    val emailRequestJs = request.body
    val id = (emailRequestJs \ "emailId").get.as[Int]

    try {
      emailRequestTransaction.delete(id)
      Ok(s"successfully deteted st: ${id}-${id.getClass} ")
    }
    catch {
      case e => Ok("failed")
    }
  }
}