# kafka-spark-hbase-pipeline

kafka-2.1.0-spark_2.4.0-hbase-2.1.0-cdh6.2.1-pipeline.jar

to run the producer:
spark-submit --class Producer /home/sathya/spark_workspace/sathyadev/HBaseTest/target/scala-2.11/kafka-2.1.0-spark_2.4.0-hbase-2.1.0-cdh6.2.1-pipeline.jar

to run the consumer:
spark-submit --class Consumer /home/sathya/spark_workspace/sathyadev/HBaseTest/target/scala-2.11/kafka-2.1.0-spark_2.4.0-hbase-2.1.0-cdh6.2.1-pipeline.jar

to verify in hbase table:
sathya@cmaster sathyadev]$ hbase shell
Java HotSpot(TM) 64-Bit Server VM warning: Using incremental CMS is deprecated and will likely be removed in a future release
HBase Shell
Use "help" to get list of supported commands.
Use "exit" to quit this interactive shell.
For Reference, please visit: http://hbase.apache.org/2.0/book.html#shell
Version 2.1.0-cdh6.2.1, rUnknown, Wed Sep 11 01:05:56 PDT 2019
Took 0.0064 seconds
hbase(main):001:0> scan 'emp'
ROW                                              COLUMN+CELL
 Key_1010                                        column=personal:country, timestamp=1584125319078, value=USA
 Key_1010                                        column=personal:name, timestamp=1584125319078, value=Mark
 Key_1010                                        column=personal:pincode, timestamp=1584125319078, value=54321
 Key_1011                                        column=personal:country, timestamp=1584125320073, value=CA
