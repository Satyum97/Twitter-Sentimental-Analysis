package parti

import java.util.HashMap

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.{SparkConf, SparkContext}
import twitter4j.Status
import org.apache.spark.streaming._
import org.apache.spark.streaming.twitter.TwitterUtils


object SparkStreamingWithTwitterAndKafka {

  def main(args: Array[String]) {

    // To handle invalid arguments
    if (args.length < 2) {
      System.out.println("Usage: SparkStreamingWithTwitterAndKafka <KafkaTopic> <keywords>")
      return
    }

    // kafka topic
    val topic = args(0).toString

    // keywords/filters required to search for a topic on twitter
    val filters = args.slice(1, args.length)

    // kafka brokers
    val kafkaBrokers = "localhost:9092,localhost:9093"

    val slideInterval = new Duration(1 * 1000)

    val windowLength = new Duration(5 * 1000)

    val timeoutJobLength = 3600 * 1000

    // initializing the spark configuration
    val sparkConfiguration = new SparkConf().
      setAppName("spark-streaming-with-twitter-and-kafka").
      setMaster(sys.env.get("spark.master").getOrElse("local[*]"))

    // initializing the spark context
    val sc = new SparkContext(sparkConfiguration)
    sc.setLogLevel("ERROR")

    // initializing the streaming context
    val streamingContext = new StreamingContext(sc, slideInterval)

    // creating a stream for getting tweets
    val tweets: DStream[Status] =
      TwitterUtils.createStream(streamingContext, None, filters)

    // Assigning the sentiment value for the filtered tweets
    val sentiments: DStream[(String)] =
      tweets.filter(x => x.getText().contains(args(1).toString)).
        map(x => (SentimentAnalyzer.mainSentiment(x.getText())).toString())

    sentiments.print()

    val streamData = sentiments.map((_, 1)).reduceByKeyAndWindow((x: Int, y: Int) => x + y, windowLength, slideInterval)

    // output on console
    streamData.print()

    // Sending data to kafka broker
    streamData.foreachRDD(rdd => {
      rdd.foreachPartition(partition => {
        val properties = new HashMap[String, Object]()
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers)
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
          "org.apache.kafka.common.serialization.StringSerializer")
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
          "org.apache.kafka.common.serialization.StringSerializer")
        val producer = new KafkaProducer[String, String](properties)

        partition.foreach(record => {
          val sentiment = record.toString
          val producerMessage = new ProducerRecord[String, String](topic, sentiment, sentiment)
          print(producerMessage)
          producer.send(producerMessage)
        })
        producer.flush()
        producer.close()
      })

    })

    // starting the stream
    streamingContext.start()

    // await the stream to end - forever
    streamingContext.awaitTerminationOrTimeout(timeoutJobLength)
  }
}
