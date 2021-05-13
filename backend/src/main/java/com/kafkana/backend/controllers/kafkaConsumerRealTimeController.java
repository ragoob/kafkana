package com.kafkana.backend.controllers;
import com.google.gson.Gson;
import com.kafkana.backend.models.consumeMessageFilter;
import com.kafkana.backend.models.kafkaCluster;
import com.kafkana.backend.repositories.kafkaClusterRepository;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.ws.rs.PathParam;
import java.time.Duration;
import java.util.*;

@RestController
@CrossOrigin()
@RequestMapping("/consumer")
public class kafkaConsumerRealTimeController {
   public static Consumer<String, String> consumer;
   public static boolean running = false;
    @Autowired
    private  SimpMessageSendingOperations messagingTemplate;
    @Autowired
    private kafkaClusterRepository kafkaClusterRepository;
    @MessageMapping("/start")
    public void startConsume(@Payload String message) throws InterruptedException {
        if(consumer != null){
            this.stopConsume("");
            Thread.sleep(1000);
        }
        running = true;
        Gson g = new Gson();
        consumeMessageFilter consumerMessage =  g.fromJson(message, consumeMessageFilter.class);
         if(consumer == null){
             consumer = this.createConsumer(consumerMessage.getClusterId());
         }
         if(consumerMessage.isFromBegaining() == true || consumerMessage.getStartOffset() > 0){
             consumer.assign(Collections.singleton(new TopicPartition(consumerMessage.getTopicName(),consumerMessage.isFromBegaining()
                     ? 0 : consumerMessage.getStartOffset())));
             consumer.seekToBeginning(consumer.assignment());
         }
         else{
             consumer.subscribe(Collections.singleton(consumerMessage.getTopicName()));
         }
        while (running){
            for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(100)))
            {
                this.messagingTemplate.convertAndSend("/topic/" + consumerMessage.getTopicName(), record.value());
            }
        }
      consumer.close();
      consumer = null;
    }

    @MessageMapping("/stop")
    public void stopConsume( String message) {
        System.out.println(message);
        running = false;
    }



    private Consumer<String, String> createConsumer(String clusterId) {
        Optional<kafkaCluster> cluster = this.kafkaClusterRepository.getById(clusterId);
        if(cluster.isEmpty()){
            throw new NullPointerException("Cluster not found");
        }
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                cluster.get().getBootStrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                "KafkaExampleConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        Consumer<String, String> consumer =  new KafkaConsumer<>(props);
        return  consumer;
    }


}
