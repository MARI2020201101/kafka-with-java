package com.mariworld.kafka.demo01;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoAssign {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerDemoAssign.class);
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG ,StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "group2");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer<String,String> consumer = new KafkaConsumer<String, String>(properties);

        TopicPartition topicPartition = new TopicPartition("first_topic",0);
        consumer.assign(Arrays.asList(topicPartition));

        long offsetToReadFrom = 15L;
        consumer.seek(topicPartition,offsetToReadFrom); //특정한 offset부터 읽어들일수있다.
        int readCount = 0;

        while(true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> record : records) {
                logger.warn("key : {} \npartition : {}\noffset : {}\nvalue : {}" , record.key(),record.partition(),record.offset(),record.value());
                readCount++;
            }
            if(readCount>=10) break;
        }


        logger.info("Exit consumer app...");

    }
}
