package services

import models.{EmailRequest, KafkaMessage}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer, OffsetAndMetadata}
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord}
import org.apache.kafka.common.TopicPartition
import play.api.mvc.{AbstractController, ControllerComponents}
import transactions.EmailRequestTransaction

import java.util
import java.util.Properties
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.{Duration, DurationInt}


class KafkaConsumerServices @Inject()
(emailSenderService: EmailSenderServices,
 emailRequestTransaction:EmailRequestTransaction,
 controllerComponents: ControllerComponents,
 ec: ExecutionContext)
  extends AbstractController(controllerComponents)
{
  def kafkaQueueService(name: String, id: Int) = {

    val name_ = name
    println(s"................Checking kafka messages from $name... ")
    val props: Properties = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val consumer = new KafkaConsumer(props)
    val topic = "notification-sender4"
    val tp0 = new TopicPartition(topic, id)
    val partitions = List[TopicPartition](tp0)
    consumer.assign(partitions.asJava)

    var kafkaMessages = new ListBuffer[KafkaMessage]()

    while (true) {
      val records = consumer.poll(100)

      for (record <- records.asScala) {
        var kafkaMessage = new KafkaMessage(record.key(), record.value(), record.partition().toString)
        kafkaMessages += (kafkaMessage)
        val eId = Integer.parseInt(record.key())
        Thread.sleep(100)

        println(s"Record: ${record.key()} - ${record.value()}")
        Thread.sleep(1000)
        val emailRequest = emailRequestTransaction.getEmailRequestById(eId)
        if(emailRequest.status == "Submitted")
        {
          val updatedEmailRequest = new EmailRequest(emailRequest.emailId, name_,
            emailRequest.responseId, emailRequest.requestId)
          emailRequestTransaction.update(updatedEmailRequest)

          val newResponseId = emailSenderService.sendEmail(emailRequest)
          Thread.sleep(100)

          if (newResponseId != "null") {
            val updatedEmailRequest = new EmailRequest(emailRequest.emailId, "Successful",
              newResponseId, emailRequest.requestId)
            emailRequestTransaction.update(updatedEmailRequest)
            Thread.sleep(500)
            println("Successfully updated emailRequest as Successful with response " + newResponseId + s"--$name")
          }
          else {
            println("Failed to send email of Id - " + eId)
          }
        }
      }
    }
  }
}