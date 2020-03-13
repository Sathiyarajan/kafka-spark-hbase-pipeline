import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor}
import org.apache.spark.sql.SparkSession
import org.apache.hadoop.hbase.client.ConnectionFactory
import org.apache.hadoop.hbase.client.Connection
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes


object Consumer {
	def main(args: Array[String]): Unit = {

		val kafkaParams = Map[String, Object](
			"bootstrap.servers" -> "cmaster.maveric.com:9092,cworker2.maveric.com:9092,cworker1.maveric.com:9092",
			"key.deserializer" -> classOf[StringDeserializer],
			"value.deserializer" -> classOf[StringDeserializer],
			"group.id" -> "use_a_separate_group_id_for_each_stream",
			"auto.offset.reset" -> "latest",
			"enable.auto.commit" -> (false: java.lang.Boolean)

		)
		val topics = Array("test")
		val sparkConf = new SparkConf().setMaster("local[*]").setAppName("KafkaTest")
		val streamingContext = new StreamingContext(sparkConf, Seconds(1))

		val kafkaStream = KafkaUtils.createDirectStream[String, String](
			streamingContext,
			PreferConsistent,
			Subscribe[String, String](topics, kafkaParams)
		)

		val spark = SparkSession.builder().master("local[8]").appName("KafkaTest").getOrCreate()
		spark.sparkContext.setLogLevel("OFF")
		val result = kafkaStream.map(record => (record.key, record.value))
		result.foreachRDD(x => {
			val cols = x.map(x => x._2.split(","))
			val arr =cols.map(x => {
				val id = x(0)
				val name = x(1)
				val country = x(2)
				val pincode = x(3)
				(id,name,country,pincode)
			})
			arr.foreachPartition { iter =>
				val conf = HBaseConfiguration.create()
				conf.set("hbase.zookeeper.quorum", "cmaster.maveric.com")
				conf.set("hbase.rootdir", "hdfs://192.168.59.17:8020/hbase")
				conf.set("hbase.zookeeper.property.clientPort", "2181")
				conf.set("zookeeper.znode.parent", "/hbase")
				conf.set("hbase.unsafe.stream.capability.enforce", "false")
				conf.set("hbase.cluster.distributed", "true")
				val conn = ConnectionFactory.createConnection(conf)
				import org.apache.hadoop.hbase.TableName
				val tableName = "emp"
				val table = TableName.valueOf(tableName)
				val HbaseTable = conn.getTable(table)
				val cfPersonal = "personal"
				iter.foreach(x => {
					val keyValue = "Key_" + x._1
					val id = new Put(Bytes.toBytes(keyValue))
					val name = x._2.toString
					val country = x._3.toString
					val pincode = x._4.toString
					id.addColumn(Bytes.toBytes(cfPersonal), Bytes.toBytes("name"), Bytes.toBytes(name))
					id.addColumn(Bytes.toBytes(cfPersonal), Bytes.toBytes("country"), Bytes.toBytes(country))
					id.addColumn(Bytes.toBytes(cfPersonal), Bytes.toBytes("pincode"), Bytes.toBytes(pincode))
					HbaseTable.put(id)
				})
				HbaseTable.close()
				conn.close()
			}
		})

		streamingContext.start()
		streamingContext.awaitTermination()
	}
}



/*
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor}
import org.apache.spark.sql.SparkSession
import org.apache.hadoop.hbase.client.ConnectionFactory
import org.apache.hadoop.hbase.client.Connection
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes


object Consumer {
  def main(args: Array[String]): Unit = {

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "cmaster.maveric.com:9092,cworker1.maveric.com:9092,cworker2.maveric.com:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "use_a_separate_group_id_for_each_stream",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)

    )

    val topics = Array("test")

    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("KafkaTest")
		sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
		sparkConf.registerKryoClasses(Array(classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable]))
    val streamingContext = new StreamingContext(sparkConf, Seconds(1))

    val kafkaStream = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    val spark = SparkSession.builder().master("local[8]").appName("KafkaTest").getOrCreate()
    spark.sparkContext.setLogLevel("OFF")
		//spark.sparkContext
			//set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
		sparkConf.registerKryoClasses(Array(classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable]))
    val result = kafkaStream.map(record => (record.key, record.value))
    result.foreachRDD(x => {
      import spark.implicits._
      val cols = x.map(x => x._2.split(","))
      val arr =cols.map(x => {
	val id = x(0)
	val name = x(1)
	val country = x(2)
	val pincode = x(3)
	(id,name,country,pincode)	
	})

	val conf = HBaseConfiguration.create()
      	conf.set("hbase.zookeeper.quorum","cmaster.maveric.com")
      	conf.set("hbase.rootdir","hdfs://192.168.59.17:8020/hbase")
      	conf.set("hbase.zookeeper.property.clientPort","2181")
      	conf.set("zookeeper.znode.parent","/hbase")
      	conf.set("hbase.unsafe.stream.capability.enforce","false")
      	conf.set("hbase.cluster.distributed","true")
	import org.apache.hadoop.hbase.client.Connection
	val conn = ConnectionFactory.createConnection(conf)
      	import org.apache.hadoop.hbase.TableName
      	val tableName = "emp"
      	val table = TableName.valueOf(tableName)
      	val HbaseTable = conn.getTable(table)
      	val cfPersonal = "personal"
	arr.foreachPartition {y =>
		y.foreach { x =>
			val keyValue = "Key_" + x._1
			val id = new Put(Bytes.toBytes(keyValue))
			val name = x._2.toString
			val country = x._3.toString
			val pincode = x._4.toString
			id.addColumn(Bytes.toBytes(cfPersonal), Bytes.toBytes("name"), Bytes.toBytes(name))
			id.addColumn(Bytes.toBytes(cfPersonal), Bytes.toBytes("country"), Bytes.toBytes(country))
			id.addColumn(Bytes.toBytes(cfPersonal), Bytes.toBytes("pincode"), Bytes.toBytes(pincode))
			HbaseTable.put(id)
		}
	}
      	HbaseTable.close()
      	conn.close()	
})
    streamingContext.start()
    streamingContext.awaitTermination()

  }
}

*/
