package transactions

import scala.concurrent.{Await, ExecutionContext, Future}
import javax.inject.Inject
import models.{EmailRequest, EmailRequestDbModel}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{Duration, DurationInt}

class EmailRequestTransaction @Inject()
(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile]
{
  import profile.api._

  class EmailRequestTable(tag: Tag) extends Table[EmailRequest](tag, "emailRequest") {
    def emailId = column[Int]("emailId", O.PrimaryKey)
    def status = column[String]("status")
    def resonseId = column[String]("resonseId")
    def requestId = column[Int]("requestId")
    def * = (emailId, status, resonseId, requestId) <> (EmailRequest.tupled, EmailRequest.unapply)
  }

  class EmailRequestModelTable(tag: Tag) extends Table[EmailRequestDbModel](tag, "emailRequest") {
    def status = column[String]("status")
    def resonseId = column[String]("resonseId")
    def requestId = column[Int]("requestId")
    def * = (status, resonseId, requestId) <> (EmailRequestDbModel.tupled, EmailRequestDbModel.unapply)
  }

  val EmailRequests = TableQuery[EmailRequestTable]
  val EmailRequestsModels = TableQuery[EmailRequestModelTable]

  def all(): Future[Seq[EmailRequest]] = {
    val res = EmailRequests.sortBy(_.emailId.desc).result
    db.run(res)
  }

  def getEmailRequestById(id: Int) =  {
    val requestQuery = EmailRequests.filter(_.emailId === id)
    println("Calling getEmailRequestById")
    val res = db.run(requestQuery.result)
    val watchDurationEmail = Duration(300, TimeUnit.MICROSECONDS)
    val resEmail = Await.result(res, watchDurationEmail)
    val email = (resEmail.head)
    val emailRequest = new EmailRequest(email.emailId, email.status, email.responseId, email.requestId)
    emailRequest
  }

  def insert(st: EmailRequestDbModel)  = {
    val insertQ = EmailRequestsModels += st
    db.run(insertQ)
    Thread.sleep(500)
    val action = sql"select max(emailId) from emailRequest".as[(Int)].headOption
    db.run(action)
  }

  def update(st: EmailRequest) = {
    val requestQuery = EmailRequests.filter(_.emailId === st.emailId)
    println("Calling update of emailRequest")
    val updateQ = requestQuery.update(st)
    val res = db.run(updateQ)
    Thread.sleep(500)
    val watchDurationEmail = Duration(5000, TimeUnit.MICROSECONDS)
    val resEmail = Await.result(res, watchDurationEmail)
    println("Returning from update of emailRequest with ID - " + resEmail )
    resEmail
  }

  def delete(id: Int): Future[Int] = {
    val requestQuery = EmailRequests.filter(_.emailId === id)
    val deleteQ = requestQuery.delete
    db.run(deleteQ)
  }
}