package kafka.demo02;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TwitterProducerV4 {
    private static final Logger logger = LoggerFactory.getLogger(TwitterProducerV4.class);

    public TwitterProducerV4() {}

    public static void main(String[] args) {
        new TwitterProducerV4().run();
    }

    public void run(){
        BlockingQueue<String> msgQueue= new LinkedBlockingQueue<String>(1000);
        Client twitterClient = createTwitterClient(msgQueue);
        twitterClient.connect();
        KafkaProducer<String, String> producer = createKafkaProducer();

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            logger.info("close kafka twitter streaming service...");
            twitterClient.stop();
            producer.close();
            logger.info("Bye!");
        }));

        while (!twitterClient.isDone()) {
            String msg = null;

            try {
                msg = msgQueue.poll(5, TimeUnit.MILLISECONDS);
                if(msg!=null){
                    ProducerRecord<String, String> record = new ProducerRecord<>("twitter_topic", msg);

                    logger.info("{}", record);
                    producer.send(record, new Callback() {
                        @Override
                        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                            logger.info("recordMetadata --> {}", recordMetadata);
                            logger.info("topic :  {} \n partition : {} \n offset : {} \n timestamp : {}",
                                    recordMetadata.topic(),
                                    recordMetadata.partition(),
                                    recordMetadata.offset(),
                                    recordMetadata.timestamp());
                        }});
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                twitterClient.stop();
            }
        }

    }

    public Client createTwitterClient(BlockingQueue<String> msgQueue){

        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

        List<String> terms = Lists.newArrayList("usa","politics","russia","war");
        hosebirdEndpoint.trackTerms(terms);

        Authentication hosebirdAuth = new OAuth1(
                TwitterAuth.AUTH_KEY.getValue(),
                TwitterAuth.AUTH_SECRET.getValue(),
                TwitterAuth.AUTH_TOKEN.getValue(),
                TwitterAuth.AUTH_TOKEN_SECRET.getValue());

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        Client hosebirdClient = builder.build();

        return hosebirdClient;
    }

    public KafkaProducer<String,String> createKafkaProducer(){
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG ,StringSerializer.class.getName());

        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG ,"true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG ,"all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG , String.valueOf(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION , "5");

        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG , "snappy");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG , "20");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG , Integer.toString(1024*32));



        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);
        return producer;

    }
}
