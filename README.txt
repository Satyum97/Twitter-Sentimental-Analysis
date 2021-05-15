Twitter Sentimental Analysis


--------------------- Files Included ---------------------
Twitter Sentimental Analysis Folder
Twitter Sentiment Analysis Summary.docx
TwitterAnalysis-assembly-0.1.jar
README.txt

------------------------------------------------------------------
Steps to create fat "TwitterAnalysis-assembly-0.1.jar" jar file:

1. Go to root directory of the project.
2. Go to sbt shell and type following command
	sbt:Twitter Sentimental Analysis> assembly

The above command will generate fat jar under target/scala-2.11/ directory by the name of TwitterAnalysis-assembly-0.1.jar.



####################################################################################################################################################################################################
PART-I INSTRUCTIONS: Spark Streaming with Twitter and Kafka

Note:
We are classifying the sentiments in three categories i.e. POSITIVE, NEUTRAL, NEGATIVE. 
We have kept the twitter authorization keys in /Assignment3/Twitter Analysis/src/main/resources folder. (Add your twitter keys there )

Arguments Required:
arg[0] ("twitter") = Kafka Topic Name
arg[1] ("KargilVijayDiwas")= Keywords/Filters required to search a topic on Twitter


Following are the commands that are required to start various services in order to do sentiment analysis on tweets via Elastic Search and Kibana:


#Command to start zookeeper:
bin/zookeeper-server-start.sh config/zookeeper.properties

#Command to start kafka:
bin/kafka-server-start.sh config/server.properties

#Command to list down all the topics :
bin/kafka-topics.sh --list --bootstrap-server localhost:9092

#Command to create topic:
bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic twitter

#Command to view messages on Consumer:
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic twitter --from-beginning

#Command to start Elastic Search:
./elasticsearch

#Command to start Kibana:
./kibana

#Command to start Logstash:
bin/logstash -f logstash-simple.conf

# Structure of logstash-simple.conf file is as follows:

input {
     kafka {
     bootstrap_servers => "localhost:9092"
     topics => ["twitter"]
     }
     }
     output {
     elasticsearch {
     hosts => ["localhost:9200"]
     index => "twitter-index"
     }
     }

Steps to run the program:

1. First build the fat jar using sbt utility
2. Run the following command :
spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.0 --class parti.SparkStreamingWithTwitterAndKafka <path> TwitterAnalysis-assembly-0.1.jar twitter "KargilVijayDiwas"
Note:
Replace <path> with the path were TwitterAnalysis-assembly-0.1.jar file is located on your system



