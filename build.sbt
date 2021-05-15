name := "Twitter Analysis"
version := "0.1"

scalaVersion := "2.11.12"


val sparkVersion = "2.4.5"
libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.11.12" % "provided",
  "org.apache.spark" %% "spark-core" % "2.4.5",
  "org.apache.spark" %% "spark-sql" % "2.4.5",
  "org.apache.spark" %% "spark-mllib" % "2.4.5",
  "org.apache.spark" %% "spark-streaming" % "2.4.5" % "provided",
  "org.apache.spark" %% "spark-hive" % "2.4.5" % "provided",
  "org.twitter4j" % "twitter4j" % "3.0.6",
  "org.twitter4j" % "twitter4j-stream" % "3.0.6",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.5" % "provided",
  "org.apache.bahir" %% "spark-streaming-twitter" % "2.3.2",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.5",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.2" artifacts (Artifact("stanford-corenlp","models"), Artifact("stanford-corenlp"))

)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}