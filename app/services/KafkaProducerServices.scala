package services

import models.EmailRequest
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import play.api.mvc.{AbstractController, ControllerComponents}
import transactions.EmailRequestTransaction

import java.util.Properties
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

class KafkaProducerServices @Inject()
(emailRequestServices:EmailRequestTransaction,
 controllerComponents: ControllerComponents,
 ec: ExecutionContext)
  extends AbstractController(controllerComponents)
{
  def insertIntoTopic(id: Int) = {

    val props: Properties = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ProducerConfig.ACKS_CONFIG, "all")
    props.put(ProducerConfig.RETRIES_CONFIG, 0)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)
    val topic = "notification-sender4"

    try {
      val record = new ProducerRecord[String, String](topic, s"$id", s"Submitted emailId - $id")
      val metadata = producer
      println("Insert Into Topic Called")
      metadata.send(record)

      val emailRequest = emailRequestServices.getEmailRequestById(id)
      val updatedEmailRequest = new EmailRequest(emailRequest.emailId, "Submitted",
        emailRequest.responseId, emailRequest.requestId)
      emailRequestServices.update(updatedEmailRequest)
      println("Successfully updated emailRequest as Submitted with " + emailRequest.emailId)
    }
    catch {
      case e: Exception => e.printStackTrace()
    }
  }
}