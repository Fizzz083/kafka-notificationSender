package services

import transactions.NotificationRequestTransaction
import models.EmailRequest
import play.api.libs.json._
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{AbstractController, ControllerComponents}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EmailSenderServices @Inject()(
                                     notificationRequestServices: NotificationRequestTransaction,
                                     mailerClient: MailerClient,
                                     controllerComponents: ControllerComponents
                                   )(
                                     implicit executionContext: ExecutionContext
                                   ) extends AbstractController(controllerComponents)
{
  def sendEmail(emailrequest: EmailRequest) = {

    val notificationRequest = notificationRequestServices.getNotificationRequestById(emailrequest.requestId)
    val emailDetails: JsValue = Json.parse(notificationRequest.requestDetails)

    val email = Email(
      subject = (emailDetails \ "subject").get.as[String],
      from = (emailDetails \ "from").get.as[String],
      to = (emailDetails \ "to").get.as[Seq[String]],
      bodyText =Some((emailDetails \ "bodyText").get.as[String]),
      bodyHtml = Some((emailDetails \ "bodyHtml").get.as[String]),
      headers = Seq(("Message-Id" , (emailDetails \ "headers").get.as[String]))
    )

    try {
      val response = mailerClient.send(email)
      println("Successfully send email with response - " + response)
      response
    }
    catch {
      case e =>
      "null"
    }
  }
}