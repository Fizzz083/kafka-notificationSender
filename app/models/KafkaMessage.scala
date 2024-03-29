package models

case class KafkaMessage(
                       val key: String,
                       val status: String,
                       val partition: String
                       )
