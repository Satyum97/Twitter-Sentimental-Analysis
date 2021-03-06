package parti

import twitter4j.Status

import scala.io.Source

object Utils {

  type Tweet = Status
  type TweetText = String
  type Sentence = Seq[String]

  private def wrapScore(s: String): String = s"[ $s ] "

  def load(resourcePath: String): Set[String] = {
    val source = Source.fromInputStream(getClass.getResourceAsStream(resourcePath))
    val words = source.getLines.toSet
    source.close()
    words
  }

  def wordsOf(tweet: TweetText): Sentence =
    tweet.split(" ")

  def toLowercase(sentence: Sentence): Sentence =
    sentence.map(_.toLowerCase)

  def keepActualWords(sentence: Sentence): Sentence =
    sentence.filter(_.matches("[a-z]+"))

  def extractWords(sentence: Sentence): Sentence =
    sentence.map(_.toLowerCase).filter(_.matches("[a-z]+"))

  def keepMeaningfulWords(sentence: Sentence, uselessWords: Set[String]): Sentence =
    sentence.filterNot(word => uselessWords.contains(word))

  def computeScore(words: Sentence, positiveWords: Set[String], negativeWords: Set[String]): Int =
    words.map(word => computeWordScore(word, positiveWords, negativeWords)).sum

  def computeWordScore(word: String, positiveWords: Set[String], negativeWords: Set[String]): Int =
    if (positiveWords.contains(word)) 1
    else if (negativeWords.contains(word)) -1
    else 0
}
