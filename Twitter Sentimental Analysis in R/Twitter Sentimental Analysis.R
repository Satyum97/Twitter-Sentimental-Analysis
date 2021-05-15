#including libraries for the project
library(igraph)
library(ggraph)
library(rtweet)
library(tidyverse)
library(tm)
library(wordcloud)
library(sentimentr)

#downloading tweets
tweetset <- search_tweets(q = "#coronavirus", n = 7000,
                               lang = "en",
                               include_rts = FALSE)


#cleaning the tweets. Removing links and handle names
tweetset$stripped_text <- gsub("http.*","",  tweetset$text)
tweetset$stripped_text <- gsub("https.*","", tweetset$stripped_text)
tweetset$stripped_text <- gsub("@[A-Za-z0-9]+","", tweetset$stripped_text)
tweetset$stripped_text <- gsub("#[A-Za-z0-9]+|@[A-Za-z0-9]+|\\w+(?:\\.\\w+)*/\\S+","",tweetset$stripped_text)
tweetset$stripped_text <- gsub("\t|\n", "",tweetset$stripped_text)
tweetset$stripped_text <- gsub("  "," ", tweetset$stripped_text)

tweetset$stripped_text[5]

# Plotting Frequency of Coronavirus Twitter Statuses
par(mfrow=c(1,2))
ts_plot(tweetset, "3 hours") +
  theme_minimal() +
  theme(plot.title = ggplot2::element_text(face = "bold")) +
  labs(
    x = NULL, y = NULL,
    title = "Frequency of Coronavirus Twitter statuses ",
    subtitle = "Twitter status (tweet) counts aggregated using three-hour intervals",
    caption = "\nSource: Data collected from Twitter's REST API via rtweet"
  )

# Plotting unique locations of users
users <- search_users("#coronavirus",n=1000)
users %>%
  count(location, sort = TRUE) %>%
  mutate(location = reorder(location,n)) %>%
  na.omit() %>%
  top_n(20) %>%
  ggplot(aes(x = location,y = n)) +
  geom_col() +
  coord_flip() +
  labs(x = "Location",
       y = "Count",
       title = "Twitter users - unique locations ")

# Building Word cloud of frequent words in Tweets
tweetset.corpus <- VCorpus(VectorSource(tweetset$stripped_text))
tweetset.corpus = tm_map(tweetset.corpus, content_transformer(tolower))
tweetset.corpus = tm_map(tweetset.corpus, removeNumbers)
tweetset.corpus = tm_map(tweetset.corpus, removePunctuation)
tweetset.corpus = tm_map(tweetset.corpus, removeWords, c("the", "and","amp", stopwords("english")))
tweetset.corpus =  tm_map(tweetset.corpus, stripWhitespace)

tweetset_dtm <- TermDocumentMatrix(tweetset.corpus)
tweetset_dtm = removeSparseTerms(tweetset_dtm, 0.99)

m <- as.matrix(tweetset_dtm)
v <- sort(rowSums(m),decreasing=TRUE)
d <- data.frame(word = names(v),freq=v)

wordcloud(words = d$word, freq = d$freq, min.freq = 1,
          max.words=200, random.order=FALSE, rot.per=0.35, 
          colors=brewer.pal(8, "Dark2"))

remove(tweetset_dtm)
remove(d)
rm(m)
rm(v)
remove(users)

# Plotting average sentiment scores of the sentences in the review
tweet_text <- data.frame(text=unlist(sapply(tweetset.corpus, `[`, "content")), 
                        stringsAsFactors=F)
tweettext <- get_sentences(tweet_text$text)
sentiment<- sentiment_by(tweettext)
qplot(sentiment$ave_sentiment,   geom="histogram",binwidth=0.1,main="Review Sentiment Histogram for Coronavirus",
      xlab = "Sentiment/Polarity Score")

# Plotting Overall Tweet Emotion
tweet_emotion <- emotion(tweettext)

tweet_emotion %>%
  ggplot()+geom_col(aes(x=emotion_type,y=emotion_count,fill=emotion_type))+xlab("Emotions")+ylab("Emotion Frequency")+
  ggtitle("Emotion varying across Tweets")


# Plotting overall Sentiment of Tweets
tweet_sent <- tweet_text %>%
  get_sentences() %>%
  sentiment() %>%
  mutate(polarity_level = ifelse(sentiment < 0.2, "Negative",
                                 ifelse(sentiment > 0.2, "Positive","Neutral")))

tweet_sent %>%
  ggplot()+geom_bar(aes(x="",y=word_count,fill=polarity_level),stat = "identity",width = 1)+
  coord_polar("y", start=0) +
  ggtitle("Sentiments varying across Tweets")+
  theme_void()

