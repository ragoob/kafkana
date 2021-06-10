package com.kafkana.backend.services;

import com.kafkana.backend.abstraction.kafkaMonitorService;
import com.kafkana.backend.models.*;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.GroupAuthorizationException;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.apache.kafka.common.errors.UnsupportedVersionException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Service
public class kafkaMonitorServiceImpl  implements kafkaMonitorService {
    private static final Logger LOG = LoggerFactory.getLogger(kafkaMonitorServiceImpl.class);
    @Override
    @Cacheable(cacheNames="summary",
            key="{#clusterIp}"
            , condition="#refresh == false")
    public clusterSummaryModel getClusterSummary(String clusterIp,boolean refresh) {
        Collection<topicModel> topics = this.getTopics(clusterIp,false,refresh);
        final var topicSummary = topics.stream()
                .map(topic -> {
                    final var summary = new clusterSummaryModel();
                    summary.setPartitionCount(topic.getPartitions().size());
                    summary.setUnderReplicatedCount(topic.getUnderReplicatedPartitions().size());
                    summary.setPreferredReplicaPercent(topic.getPreferredReplicaPercent());
                    topic.getPartitions()
                            .forEach(partition -> {
                                if (partition.getLeader() != null) {
                                    summary.addBrokerLeaderPartition(partition.getLeader().getId());
                                }
                                if (partition.getPreferredLeader() != null) {
                                    summary.addBrokerPreferredLeaderPartition(partition.getPreferredLeader().getId());
                                }
                            });
                    return summary;
                })
                .reduce((s1, s2) -> {
                    s1.setPartitionCount(s1.getPartitionCount() + s2.getPartitionCount());
                    s1.setUnderReplicatedCount(s1.getUnderReplicatedCount() + s2.getUnderReplicatedCount());
                    s1.setPreferredReplicaPercent(s1.getPreferredReplicaPercent() + s2.getPreferredReplicaPercent());
                    s2.getBrokerLeaderPartitionCount().forEach(s1::addBrokerLeaderPartition);
                    s2.getBrokerPreferredLeaderPartitionCount().forEach(s1::addBrokerPreferredLeaderPartition);
                    return s1;
                })
                .orElseGet(clusterSummaryModel::new);
        topicSummary.setTopicCount(topics.size());
        topicSummary.setPreferredReplicaPercent(topics.isEmpty() ? 0
                : topicSummary.getPreferredReplicaPercent() / topics.size());
        topicSummary.setBrokerCount(topicSummary.getExpectedBrokerIds().size());
        topicSummary.setTimeStamp(new Date());
        return topicSummary;
    }
    @Override
    @Cacheable(cacheNames="topics",
            key="{#clusterIp}"
            , condition="#refresh == false")
    public List<topicModel> getTopics(String clusterIp, boolean showDefaultConfig,boolean refresh) {
        final  var kafkaConsumer= createConsumer(clusterIp);
        final  var admin = getAdminClient(clusterIp);
        try{

            final var topics = getTopicMetadata(kafkaConsumer,admin,showDefaultConfig).values().stream()
                    .sorted(Comparator.comparing(topicModel::getName))
                    .collect(Collectors.toList());
            kafkaConsumer.close();
            admin.close();
            return topics;
        } catch (Exception ex){
            kafkaConsumer.close();
            admin.close();
            throw ex;
        }
    }
    @Override
    public Optional<topicModel> getTopic(String topic,String clusterIp,boolean showDefaultConfig) {
        final  var kafkaConsumer= createConsumer(clusterIp);
        final  var admin = getAdminClient(clusterIp);
        
        try{
            final var topicModel = Optional.ofNullable(getTopicMetadata(kafkaConsumer,admin,showDefaultConfig).get(topic));
            topicModel.ifPresent(model -> model.setPartitions(getTopicPartitionSizes(model,kafkaConsumer)));
            kafkaConsumer.close();
            admin.close();
            return topicModel;
        } catch (Exception ex){
            kafkaConsumer.close();
            admin.close();
            return Optional.empty();
        }
    }
    @Override
    @Cacheable(cacheNames="consumers",
            key="{#clusterIp}"
            , condition="#refresh == false")
    public List<consumerModel> getConsumers(Collection<topicModel> topicModels,String clusterIp,boolean refresh) {
        final  var admin = getAdminClient(clusterIp);
        final var topics = topicModels.stream().map(topicModel::getName).collect(Collectors.toSet());
         try{
             final var consumerGroupOffsets = getConsumerOffsets(topics,admin);
             admin.close();
             return convert(consumerGroupOffsets, topicModels);
         }catch (Exception ex){
             admin.close();
             return  new ArrayList<>();
         }
    }


    @Override
    public  List<messageModel> getMessages(String topic,String clusterIp,int size,long start, long end){
         List<messageModel> messages = new ArrayList<>();
        Consumer<String, String> kafkaConsumer =this.createConsumer(clusterIp);
        try{
            TopicPartition partition = new TopicPartition(topic, 0);
            long beginningOffset = kafkaConsumer.offsetsForTimes(
                    Collections.singletonMap(partition, start))
                    .get(partition).offset();

            kafkaConsumer.assign(Collections.singleton(partition)); // must assign before seeking
            kafkaConsumer.seek(partition, beginningOffset);
            long startTime = System.currentTimeMillis();
            while ( messages.size() < size && (System.currentTimeMillis()-startTime)<1000){
                for (ConsumerRecord<String, String> record : kafkaConsumer.poll(Duration.ofMillis(200))) {

                    if(record.timestamp() >= start  && record.timestamp() <= end && messages.size() < size){
                        messages.add(new messageModel(record.partition(),record.offset(),record.value(),record.key(),
                                headersToMap(record.headers())
                                ,new Date(record.timestamp())));
                    }

                }
            }
            kafkaConsumer.close();
            return  messages;
        }catch (Exception ex){
            kafkaConsumer.close();
            return  new ArrayList<>();
        }
    }

    @Override
    public  List<messageModel> getMessages(String topic,String clusterIp,int size,long start){
        List<messageModel> messages = new ArrayList<>();
        Consumer<String, String> kafkaConsumer =this.createConsumer(clusterIp);
        try{
            TopicPartition partition = new TopicPartition(topic, 0);
            long beginningOffset = kafkaConsumer.offsetsForTimes(
                    Collections.singletonMap(partition, start))
                    .get(partition).offset();

            kafkaConsumer.assign(Collections.singleton(partition)); // must assign before seeking
            kafkaConsumer.seek(partition, beginningOffset);
            long startTime = System.currentTimeMillis();
            while ((System.currentTimeMillis()-startTime)<1000){
                for (ConsumerRecord<String, String> record : kafkaConsumer.poll(Duration.ofMillis(200))) {
                    if(messages.size() < size) {
                        messages.add(new messageModel(record.partition(), record.offset(), record.value(), record.key(),
                                headersToMap(record.headers())
                                , new Date(record.timestamp())));
                    }
                }
            }
            kafkaConsumer.close();
            return  messages;
        }catch (Exception ex){
            kafkaConsumer.close();
            return  new ArrayList<>();
        }
    }

    @Override
    public  List<messageModel> getMessagesUntilTime(String topic,String clusterIp,int size,long end){
        List<messageModel> messages = new ArrayList<>();
        Consumer<String, String> kafkaConsumer =this.createConsumer(clusterIp);
        try{
            TopicPartition partition0 = new TopicPartition(topic, 0);
            TopicPartition partition1 = new TopicPartition(topic, 1);
            TopicPartition partition3 = new TopicPartition(topic, 3);
            Collection<TopicPartition> partitions = new HashSet<>();
            partitions.add(partition0);
            partitions.add(partition1);
            partitions.add(partition3);
            kafkaConsumer.assign(partitions); // must assign before seeking
            kafkaConsumer.seekToBeginning(partitions);

            long startTime = System.currentTimeMillis();
            while (messages.size() < size && (System.currentTimeMillis()-startTime)<1000){
                for (ConsumerRecord<String, String> record : kafkaConsumer.poll(Duration.ofMillis(200))) {

                    if(record.timestamp() <= end && messages.size() < size){
                        messages.add(new messageModel(record.partition(),record.offset(),record.value(),record.key(),
                                headersToMap(record.headers())
                                ,new Date(record.timestamp())));
                    }

                }
            }
            kafkaConsumer.close();
            return  messages;
        }catch (Exception ex){
            kafkaConsumer.close();
            return  new ArrayList<>();
        }
    }

    @Override
    public  List<messageModel> getLatestMessages(String topic,String clusterIp,int size){
        List<messageModel> messages = new ArrayList<>();
       final  var records = getLatestRecords(topic,size,clusterIp);
       for(ConsumerRecord<String, String> record : records){
           if(messages.size()< size){
               messages.add(new messageModel(record.partition(),record.offset(),record.value(),record.key(),
                       headersToMap(record.headers())
                       ,new Date(record.timestamp())));
           }
       }
       return  messages;
    }


    private  List<ConsumerRecord<String, String>> getLatestRecords(String topic,
                                                                   int count,String clusterIp) {
        Consumer<String, String> kafkaConsumer =this.createConsumer(clusterIp);
       try{
           final var partitionInfoSet = kafkaConsumer.partitionsFor(topic);
           final var partitions = partitionInfoSet.stream()
                   .map(partitionInfo -> new TopicPartition(partitionInfo.topic(),
                           partitionInfo.partition()))
                   .collect(Collectors.toList());
           kafkaConsumer.assign(partitions);
           final var latestOffsets = kafkaConsumer.endOffsets(partitions);

           for (var partition : partitions) {
               final var latestOffset = Math.max(0, latestOffsets.get(partition) - 1);
               kafkaConsumer.seek(partition, Math.max(0, latestOffset - count));
           }

           final Map<TopicPartition, List<ConsumerRecord<String, String>>> rawRecords
                   = partitions.stream().collect(Collectors.toMap(p -> p , p -> new ArrayList<>(count)));

           long startTime = System.currentTimeMillis();
           while ((System.currentTimeMillis()-startTime)<1000) {
               final var polled = kafkaConsumer.poll(Duration.ofMillis(200));
               for (var partition : polled.partitions()) {
                   var records = polled.records(partition);
                   if (!records.isEmpty()) {
                       rawRecords.get(partition).addAll(records);
                   }
               }
           }

           System.out.println("Closing Kafka consumer ...");
           kafkaConsumer.close();
           this.deleteConsumer(clusterIp,kafkaConsumer.groupMetadata().groupId());
           var flatMapList =  rawRecords
                   .values()
                   .stream()
                   .flatMap(Collection::stream)
                   .collect(Collectors.toList());
           System.out.println("Final Size " + flatMapList.size());
           return  flatMapList;
       }catch (Exception ex){
           kafkaConsumer.close();
           this.deleteConsumer(clusterIp,kafkaConsumer.groupMetadata().groupId());
           return  new ArrayList<>();
       }
    }

    private Map<String, topicModel> getTopicMetadata( Consumer<String,String> consumer,AdminClient adminClient,boolean showDefaultConfig,String... topics) {
        final var topicsMap = getTopicInformation(topics,consumer);
        final var retrievedTopicNames = topicsMap.keySet();
        Map<String, Config> topicConfigs = new HashMap<>();
        try {
            topicConfigs = describeTopicConfigs(retrievedTopicNames,adminClient);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (var topicModel : topicsMap.values()) {
            final var config = topicConfigs.get(topicModel.getName());
            if (config != null) {
                final var configMap = new TreeMap<String, String>();
                for (var configEntry : config.entries()) {
                    if ((configEntry.source() != ConfigEntry.ConfigSource.DEFAULT_CONFIG &&
                            configEntry.source() != ConfigEntry.ConfigSource.STATIC_BROKER_CONFIG) || showDefaultConfig) {
                        configMap.put(configEntry.name(), configEntry.value());
                    }
                }
                topicModel.setConfig(configMap);
            } else {
                LOG.warn("Missing config for topic {}", topicModel.getName());
            }
        }
        return topicsMap;
    }

    private Map<Integer, topicPartitionModel> getTopicPartitionSizes(topicModel topic,Consumer<String,String> consumer) {
        return getPartitionSize(topic.getName(),consumer);
    }

    private Consumer<String, String> createConsumer(String clusterIp) {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                clusterIp);
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                "KAFKANA_UI_MONITORING");
        props.put(ConsumerConfig.CLIENT_ID_CONFIG,
                "KAFKANA_UI_MONITORING-CONUMER-" + uuidAsString);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        return new KafkaConsumer<>(props);
    }

    private AdminClient getAdminClient(String clusterIp) {

        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,clusterIp);
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();
        config.put(AdminClientConfig.CLIENT_ID_CONFIG,"ADMIN_CLIENT-" + uuidAsString);
        return AdminClient.create(config);
    }

    synchronized Map<Integer, topicPartitionModel> getPartitionSize(String topic,Consumer<String,String> kafkaConsumer) {

        final var partitionInfoSet = kafkaConsumer.partitionsFor(topic);
        kafkaConsumer.assign(partitionInfoSet.stream()
                .map(partitionInfo -> new TopicPartition(partitionInfo.topic(),
                        partitionInfo.partition()))
                .collect(Collectors.toList()));

        kafkaConsumer.poll(Duration.ofMillis(0));
        final Set<TopicPartition> assignedPartitionList = kafkaConsumer.assignment();
        final topicModel topicModel = getTopicInfo(topic,kafkaConsumer);
        final Map<Integer, topicPartitionModel> partitionMap = topicModel.getPartitionMap();

        kafkaConsumer.seekToBeginning(assignedPartitionList);
        assignedPartitionList.forEach(topicPartition -> {
            final topicPartitionModel topicPartitionModel = partitionMap.get(topicPartition.partition());
            final long startOffset = kafkaConsumer.position(topicPartition);
            LOG.debug("topic: {}, partition: {}, startOffset: {}", topicPartition.topic(), topicPartition.partition(), startOffset);
            topicPartitionModel.setFirstOffset(startOffset);
        });

        kafkaConsumer.seekToEnd(assignedPartitionList);
        assignedPartitionList.forEach(topicPartition -> {
            final long latestOffset = kafkaConsumer.position(topicPartition);
            LOG.debug("topic: {}, partition: {}, latestOffset: {}", topicPartition.topic(), topicPartition.partition(), latestOffset);
            final topicPartitionModel partitionModel = partitionMap.get(topicPartition.partition());
            partitionModel.setSize(latestOffset);
        });


        return partitionMap;
    }

    private topicModel getTopicInfo(String topic,Consumer<String,String> kafkaConsumer) {
        final var partitionInfoList = kafkaConsumer.partitionsFor(topic);
        final var topicModel = new topicModel(topic);
        final var partitions = new TreeMap<Integer, topicPartitionModel>();

        for (var partitionInfo : partitionInfoList) {
            final var topicPartitionModel = new topicPartitionModel(partitionInfo.partition());
            final var inSyncReplicaIds = Arrays.stream(partitionInfo.inSyncReplicas()).map(Node::id).collect(Collectors.toSet());
            final var offlineReplicaIds = Arrays.stream(partitionInfo.offlineReplicas()).map(Node::id).collect(Collectors.toSet());

            for (var node : partitionInfo.replicas()) {
                final var isInSync = inSyncReplicaIds.contains(node.id());
                final var isOffline = offlineReplicaIds.contains(node.id());
                topicPartitionModel.addReplica(new topicPartitionModel.PartitionReplica(node.id(), isInSync, false, isOffline));
            }

            final var leader = partitionInfo.leader();
            if (leader != null) {
                topicPartitionModel.addReplica(new topicPartitionModel.PartitionReplica(leader.id(), true, true, false));
            }
            partitions.put(partitionInfo.partition(), topicPartitionModel);
        }

        topicModel.setPartitions(partitions);

        return topicModel;
    }

    synchronized Map<String, topicModel> getTopicInformation(String[] topics, Consumer<String,String> kafkaConsumer ) {
        final var topicSet = kafkaConsumer.listTopics().keySet();

        if (topics.length == 0) {
            topics = Arrays.copyOf(topicSet.toArray(), topicSet.size(), String[].class);
        }
        final var topicModelMap = new HashMap<String, topicModel>(topics.length, 1f);

        for (var topic : topics) {
            if (topicSet.contains(topic)) {
                topicModelMap.put(topic, getTopicInfo(topic,kafkaConsumer));
            }
        }
        return topicModelMap;
    }

    Map<String, Config> describeTopicConfigs(Set<String> topicNames,AdminClient adminClient )  {
        final var resources = topicNames.stream()
                .map(topic -> new ConfigResource(ConfigResource.Type.TOPIC, topic))
                .collect(Collectors.toList());
        final var result = adminClient.describeConfigs(resources);
        final Map<String, Config> configsByTopic;
        try {
            final var allConfigs = result.all().get();
            configsByTopic = new HashMap<>(allConfigs.size(), 1f);
            for (var entry : allConfigs.entrySet()) {
                configsByTopic.put(entry.getKey().name(), entry.getValue());
            }
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof UnsupportedVersionException) {
                return Map.of();
            } else if (e.getCause() instanceof TopicAuthorizationException) {
                return Map.of();
            }
            throw new RuntimeException(e);
        }

        return configsByTopic;
    }

    Set<String> listConsumerGroups(AdminClient adminClient) {
        final Collection<ConsumerGroupListing> groupListing;
        try {
            groupListing = adminClient.listConsumerGroups().all().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


        return groupListing.stream().map(ConsumerGroupListing::groupId).collect(Collectors.toSet());
    }

    private ConsumerGroupOffsets resolveOffsets(String groupId,AdminClient adminClient) {
        return new ConsumerGroupOffsets(groupId,listConsumerGroupOffsetsIfAuthorized(groupId,adminClient));
    }

    Map<TopicPartition, OffsetAndMetadata> listConsumerGroupOffsetsIfAuthorized(String groupId,AdminClient adminClient) {
        final var offsets = adminClient.listConsumerGroupOffsets(groupId);
        try {
            return offsets.partitionsToOffsetAndMetadata().get();
        } catch (InterruptedException | ExecutionException e) {
            if (e.getCause() instanceof GroupAuthorizationException) {
                LOG.info("Not authorized to view consumer group {}; skipping", groupId);
                return Collections.emptyMap();
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private List<ConsumerGroupOffsets> getConsumerOffsets(Set<String> topics,AdminClient adminClient) {
        final var consumerGroups = listConsumerGroups(adminClient);
        return consumerGroups.stream()
                .map(c-> resolveOffsets(c,adminClient))
                .map(offsets -> offsets.forTopics(topics))
                .filter(not(ConsumerGroupOffsets::isEmpty))
                .collect(Collectors.toList());
    }

    private record ConsumerGroupOffsets(String groupId,
                                        Map<TopicPartition, OffsetAndMetadata> offsets) {

        boolean isEmpty() {
            return offsets.isEmpty();
        }

        ConsumerGroupOffsets forTopics(Set<String> topics) {
            final var filteredOffsets = offsets.entrySet().stream()
                    .filter(e -> e.getValue() != null)
                    .filter(e -> topics.contains(e.getKey().topic()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new ConsumerGroupOffsets(groupId, filteredOffsets);
        }

        @Override
        public String toString() {
            return ConsumerGroupOffsets.class.getSimpleName() + " [groupId=" + groupId + ", offsets=" + offsets + "]";
        }
    }

    private static List<consumerModel> convert(List<ConsumerGroupOffsets> consumerGroupOffsets, Collection<topicModel> topicModels) {
        final var topicMap = topicModels.stream().collect(Collectors.toMap(topicModel::getName, Function.identity()));
        final var groupTopicPartitionOffsetMap = new TreeMap<String, Map<String, Map<Integer, Long>>>();
        for (var consumerGroupOffset : consumerGroupOffsets) {
            final var groupId = consumerGroupOffset.groupId;

            for (var topicPartitionOffset : consumerGroupOffset.offsets.entrySet()) {
                final var topic = topicPartitionOffset.getKey().topic();
                final var partition = topicPartitionOffset.getKey().partition();
                final var offset = topicPartitionOffset.getValue().offset();
                groupTopicPartitionOffsetMap
                        .computeIfAbsent(groupId, __ -> new TreeMap<>())
                        .computeIfAbsent(topic, __ -> new TreeMap<>())
                        .put(partition, offset);
            }
        }

        final var consumerModels = new ArrayList<consumerModel>(consumerGroupOffsets.size());
        for (var groupTopicPartitionOffset : groupTopicPartitionOffsetMap.entrySet()) {
            final var groupId = groupTopicPartitionOffset.getKey();
            final var consumerModel = new consumerModel(groupId);
            consumerModels.add(consumerModel);

            for (var topicPartitionOffset : groupTopicPartitionOffset.getValue().entrySet()) {
                final var topic = topicPartitionOffset.getKey();
                final var consumerTopicModel = new consumerTopicModel(topic);
                consumerModel.addTopic(consumerTopicModel);

                for (var partitionOffset : topicPartitionOffset.getValue().entrySet()) {
                    final var partition = partitionOffset.getKey();
                    final var offset = partitionOffset.getValue();
                    final var offsetModel = new consumerPartitionModel(groupId, topic, partition);
                    consumerTopicModel.addOffset(offsetModel);
                    offsetModel.setOffset(offset);
                    final var topicModel = topicMap.get(topic);
                    final var topicModelPartition = topicModel.getPartition(partition);
                    offsetModel.setSize(topicModelPartition.map(topicPartitionModel::getSize).orElse(0L));
                    offsetModel.setFirstOffset(topicModelPartition.map(topicPartitionModel::getFirstOffset).orElse(0L));
                }
            }
        }

        return consumerModels;
    }

    private static Map<String, String> headersToMap(Headers headers) {
        final var map = new TreeMap<String, String>();
        for (var header : headers) {
            final var value = header.value();
            map.put(header.key(), (value == null) ? null : new String(value));
        }
        return map;
    }

    private   void  deleteConsumer(String clusterIp, String id){
        final AdminClient admin = getAdminClient(clusterIp);
        admin.deleteConsumerGroups(Collections.singleton(id));
        admin.close();
    }
}
