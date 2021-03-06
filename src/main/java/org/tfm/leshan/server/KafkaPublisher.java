package org.tfm.leshan.server;

import com.google.gson.Gson;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tfm.leshan.server.model.Dht22;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private final KafkaProducer<Integer, String> producer;
    private final String topic;
    private final Boolean isAsync;

    public KafkaPublisher(String topic, String server, int port, String clientId, Boolean isAsync) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", server + ":" + port);
        properties.put("client.id", clientId);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new org.apache.kafka.clients.producer.KafkaProducer<Integer, String>(properties);
        this.topic = topic;
        this.isAsync = isAsync;
    }

    public void sendMessage(double temp, double humidity, String deviceId){

        // No recogemos los valores iniciales
        if (temp == 0.0 || humidity == 0.0) return;

        Dht22 sensor = new Dht22();
        sensor.setDeviceId(deviceId);
        sensor.setTemperature(temp);
        sensor.setHumidity(humidity);

        sensor.setTimestamp(new Date().getTime());
        //Gson json = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        Gson json = new Gson();
        String messageStr = json.toJson(sensor);
        long startTime = System.currentTimeMillis();

        try {
            producer.send(new ProducerRecord<>(topic,
                    messageStr)).get();
            log.info("Sent message: " + messageStr + ")");
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            // handle the exception
        }
            /*if (isAsync) { // Send asynchronously
                producer.send(new ProducerRecord<>(topic,
                        messageStr), new DemoCallBack(startTime, messageNo, messageStr));
            } else { // Send synchronously
                try {
                    producer.send(new ProducerRecord<>(topic,
                            messageNo,
                            messageStr)).get();
                    log.info("Sent message: (" + messageNo + ", " + messageStr + ")");
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    // handle the exception
                }
            }
            ++messageNo;*/

    }

}
