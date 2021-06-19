package com.kafkana.backend.services;
import com.kafkana.backend.abstraction.kafkaAdminService;
import com.kafkana.backend.models.*;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class kafkaAdminServiceImpl implements kafkaAdminService {
    @Override
    public void create(createTopicModel model,String clusterIp) {
        try (final AdminClient admin = getAdminClient(clusterIp)){
            NewTopic newTopic = new NewTopic(model.getTopicName(),model.getPartitions(),model.getReplication());
            newTopic.configs(model.getConfigurations());
            admin.createTopics(Collections.singleton(newTopic));
        }

    }

    private AdminClient getAdminClient(String clusterIp) {
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, clusterIp);
        config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG,20000);
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        config.put(AdminClientConfig.CLIENT_ID_CONFIG,"ADMIN_CLIENT-" + uuidAsString);

        return AdminClient.create(config);
    }

    @Override
    public void delete(createTopicModel model,String clusterIp) {
        try (final AdminClient admin = getAdminClient(clusterIp)){
            admin.deleteTopics(Collections.singleton(model.getTopicName()));
            admin.listTopics();
        }

    }

    @Override
    public ArrayList<brokers> getConfig(String clusterIp) {
        ArrayList<brokers> configs = new ArrayList<>();
        try (final AdminClient admin = getAdminClient(clusterIp)){
         final  var clusterDescription =  admin.describeCluster();
           final  var controller = clusterDescription.controller().get();
            clusterDescription.nodes().get().forEach(c-> {
             HashMap<String,String> config = getBrokersConfig(Integer.toString(c.id()),admin);
             brokers brokers = new brokers(c.id(),c.host(),c.port(),c.rack(),config,controller.id() == c.id());
              configs.add(brokers);
            });
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return  configs;
    }

    @Override
    public HashMap<String,String> getConfig(String clusterIp,String id) {
        try (final AdminClient admin = getAdminClient(clusterIp)) {
            return getBrokersConfig(id,admin);
        }
    }

    @Override
    public ArrayList<brokers> getBrokers(String clusterIp) {
        ArrayList<brokers> nodes = new ArrayList<>();

        try (final AdminClient admin = getAdminClient(clusterIp)){
            final var clusterDescription = admin.describeCluster();
            final  var controller = clusterDescription.controller().get();
            clusterDescription.nodes().get().forEach(c-> {
                HashMap<String,String> config = getBrokersConfig(Integer.toString(c.id()),admin);
                nodes.add(new brokers(c.id(),c.host(),c.port(),c.rack(),config,controller.id() == c.id()));
            });
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return nodes;
    }

    @Override
    public boolean IsHealth(String clusterIp) {
          boolean isHealthy = false;
        try (AdminClient admin = getAdminClient(clusterIp)) {
            admin.listTopics();
            isHealthy = true;
        } catch (Exception e) {
            System.out.println("Err.. " + e.getMessage());

        }

        return isHealthy;



    }

     @Override
     public  void  deleteConsumer(String clusterIp, String id){
         final AdminClient admin = getAdminClient(clusterIp);
         admin.deleteConsumerGroups(Collections.singleton(id));
         admin.close();
     }




    private  HashMap<String,String> getBrokersConfig(String id, AdminClient admin){
        ConfigResource configResource = new ConfigResource(ConfigResource.Type.BROKER, id);
        try {
            HashMap<String,String> config = new HashMap<>();
             admin
                    .describeConfigs(Collections.singletonList(configResource))
                    .values()
                    .get(configResource)
                    .get()
                     .entries()
                     .forEach(configEntry -> {
                         config.put(configEntry.name(),configEntry.value());
                     });
            return  config;

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return  null;

    }
}
