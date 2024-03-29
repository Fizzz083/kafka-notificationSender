package services

import com.google.inject.Inject
import play.api.Environment
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Singleton


class ConsumerThread @Inject() (
                           kafkaConsumerServices: KafkaConsumerServices,
                           name: String,
                           id: Int
                         ) extends Thread {
  var name_ = name
  override def run(): Unit = {
    try{
      while(true) {
        println("Running thread - "+name_)
        kafkaConsumerServices.kafkaQueueService(name_, id)
      }
    }
    catch{
      case e => println(s"Consumer thread-$name_ creates problem")
    }
  }
}

@Singleton
class StartUpService @Inject()(kafkaConsumerServices: KafkaConsumerServices,
                               lifeCycle: ApplicationLifecycle) {
  @Inject()
  def onStart(env: Environment)(implicit ec: ExecutionContext): Unit = {
    for(i <- 0 to 4){
     val th = new ConsumerThread(kafkaConsumerServices, s"thread-$i", i)
     th.start()
    }
  }

  lifeCycle.addStopHook{ () =>
    println("Successfully done startUpServices")
    Future.successful(())
  }
}