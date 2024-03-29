package transactions



import scala.concurrent.{Await, ExecutionContext, Future}
import javax.inject.Inject
import models.{NotificationRequest, NotificationRequestDbModel}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{Duration}
import scala.language.postfixOps

class NotificationRequestTransaction @Inject()
    (protected val dbConfigProvider: DatabaseConfigProvider)
    (implicit executionContext: ExecutionContext)
    extends HasDatabaseConfigProvider[JdbcProfile]
{
  import profile.api._

  class NotificationRequestTable(tag: Tag) extends Table[NotificationRequest](tag, "notificationRequest") {
    def requestId = column[Int]("requestId", O.PrimaryKey)
    def requestType = column[String]("requestType")
    def requestDetails = column[String]("requestDetails")
    def requestDate = column[String]("requestDate")
    def * = (requestId, requestType, requestDetails, requestDate) <> (NotificationRequest.tupled, NotificationRequest.unapply)
  }

  class NotificationRequestModelTable(tag: Tag) extends Table[NotificationRequestDbModel](tag, "notificationRequest") {
    def requestType = column[String]("requestType")
    def requestDetails = column[String]("requestDetails")
    def requestDate = column[String]("requestDate")
    def * = (requestType, requestDetails, requestDate) <> (NotificationRequestDbModel.tupled, NotificationRequestDbModel.unapply)
  }

  val NotificationRequests = TableQuery[NotificationRequestTable]
  val NotificationRequestsModels = TableQuery[NotificationRequestModelTable]

  def all(): Future[Seq[NotificationRequest]] = {
    db.run(NotificationRequests.result)
  }

  def getNotificationRequestById(id: Int)=  {
    val requestQuery = NotificationRequests.filter(_.requestId === id)

    val res = db.run(requestQuery.result)
    val watchDurationEmail = Duration(5000, TimeUnit.MICROSECONDS)

    val resEmail = Await.result(res, watchDurationEmail)
    var email = (resEmail.head)
    val emailRequest = new NotificationRequest(email.requestId, email.requestType, email.requestDetails, email.requestDate)
    emailRequest
  }

  def insert(st: NotificationRequestDbModel)= {
    val insertQ = NotificationRequestsModels += st
    db.run(insertQ)
    Thread.sleep(500)
    val action = sql"select max(requestId) from notificationRequest".as[(Int)].headOption
    db.run(action)
  }

  def update(st: NotificationRequest): Future[Int] = {
    val requestQuery = NotificationRequests.filter(_.requestId === st.requestId)
    val updateQ = requestQuery.update(st)
    db.run(updateQ)
  }

  def delete(id: Int): Future[Int] = {
    val requestQuery = NotificationRequests.filter(_.requestId === id)
    val deleteQ = requestQuery.delete
    db.run(deleteQ)
  }
}