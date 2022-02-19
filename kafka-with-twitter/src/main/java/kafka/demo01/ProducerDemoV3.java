package kafka.demo01;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoV3 {

    private static final Logger logger = LoggerFactory.getLogger(ProducerDemoV3.class);
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG ,StringSerializer.class.getName());

        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);

        for (int i = 0; i < 10; i++) {

            String topic ="first_topic";
            String key ="id [" + i + "]";
            String value ="hello world [" + i + "]";
            ProducerRecord<String,String> record = new ProducerRecord<>(topic,key,value);
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    logger.info("recordMetadata --> {}", recordMetadata);
                    logger.info("topic :  {} \n partition : {} \n offset : {} \n timestamp : {}",
                            recordMetadata.topic(),
                            recordMetadata.partition(),
                            recordMetadata.offset(),
                            recordMetadata.timestamp());
                }
            });
            logger.warn("record --> {}", record);
        }

        producer.flush();
        producer.close();
    }
}
