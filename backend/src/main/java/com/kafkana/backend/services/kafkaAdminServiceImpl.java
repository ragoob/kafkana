package com.kafkana.backend.services;
import com.kafkana.backend.abstraction.kafkaAdminService;
import com.kafkana.backend.models.*;
import com.kafkana.backend.repositories.kafkaClusterRepository;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class kafkaAdminServiceImpl implements kafkaAdminService {

    @Override
    public void create(createTopicModel model,String clusterId) {
        try (final AdminClient admin = getAdminClient(clusterId)){
            NewTopic newTopic = new NewTopic(model.getTopicName(),model.getPartitions(),model.getReplication());
            newTopic.configs(model.getConfigurations());
            admin.createTopics(Collections.singleton(newTopic));
        }

    }

    private AdminClient getAdminClient(String clusterId) {
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, clusterId);
        config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        AdminClient admin = AdminClient.create(config);

        return  admin;
    }

    @Override
    public void delete(createTopicModel model,String clusterId) {
        try (final AdminClient admin = getAdminClient(clusterId)){
            admin.deleteTopics(Collections.singleton(model.getTopicName()));
            admin.listTopics();
        }

    }

    @Override
    public ArrayList<brokers> getConfig(String clusterId) {
        ArrayList<brokers> configs = new ArrayList<>();
        try (final AdminClient admin = getAdminClient(clusterId)){
         final  var clusterDescription =  admin.describeCluster();
           final  var controller = clusterDescription.controller().get();
            clusterDescription.nodes().get().forEach(c-> {
             HashMap<String,String> config = getBrokersConfig(Integer.toString(c.id()),admin);
             brokers brokers = new brokers(c.id(),c.host(),c.port(),c.rack(),config,controller.id() == c.id());
              configs.add(brokers);
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return  configs;
    }

    @Override
    public HashMap<String,String> getConfig(String clusterId,String id) {
        try (final AdminClient admin = getAdminClient(clusterId)) {
            return getBrokersConfig(id,admin);
        }
    }

    @Override
    public ArrayList<brokers> getBrokers(String clusterId) {
        ArrayList<brokers> nodes = new ArrayList<>();

        try (final AdminClient admin = getAdminClient(clusterId)){
            final var clusterDescription = admin.describeCluster();
            final  var controller = clusterDescription.controller().get();
            clusterDescription.nodes().get().forEach(c-> {
                HashMap<String,String> config = getBrokersConfig(Integer.toString(c.id()),admin);
                nodes.add(new brokers(c.id(),c.host(),c.port(),c.rack(),config,controller.id() == c.id()));
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return nodes;
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

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

         return  null;

    }
}
