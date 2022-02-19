package com.mariworld.kafka.demo04;

import com.google.gson.JsonParser;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.stream.Stream;

public class StreamsFilterTweets {
    private static final Logger logger = LoggerFactory.getLogger(StreamsFilterTweets.class);
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG,"demo-kafka-streams");
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG ,Serdes.StringSerde.class.getName());

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String,String> inputTopic = streamsBuilder.stream("twitter_topic");
        KStream<String,String> filteredStream = inputTopic.filter(
                (key,tweets) -> extractUserFollowers(tweets) > 1000000);
        filteredStream.to("important_tweets");

        KafkaStreams kafkaStreams = new KafkaStreams(
                streamsBuilder.build(), properties
        );

        kafkaStreams.start();

    }

    private static JsonParser jsonParser = new JsonParser();
    private static Integer extractUserFollowers(String tweetJson) {
        try{
            return jsonParser.parse(tweetJson)
                    .getAsJsonObject()
                    .get("user")
                    .getAsJsonObject()
                    .get("followers_count")
                    .getAsInt();
        }catch (Exception e){
            logger.error(e.getMessage());
            return 0;
        }
    }

}
