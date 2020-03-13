import java.util.Properties
import scala.io.Source
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object Producer {
  def main(args: Array[String]): Unit = {

    val topic = "test"
    val brokers = "cmaster.maveric.com:9092,cworker1.maveric.com:9092,cworker2.maveric.com:9092"
    val props = new Properties()
    props.put("bootstrap.servers", brokers)
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    val producer = new KafkaProducer[String, String](props)

    val data = Source.fromFile("/home/eresht/spark_workspace/HBaseTest/data/Employee.csv")
    val strs = data.getLines().map(x =>{
      val w = x.split(",")
      val str = w(0)+","+w(1)+","+w(2)+","+w(3)
      str
    })
    for(i<-strs){
      producer.send(new ProducerRecord[String, String](topic, i.toString))
      println("Message sent: " + i)
      Thread.sleep(1000)
    }
  }
}

