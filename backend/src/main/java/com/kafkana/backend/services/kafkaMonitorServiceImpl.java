package com.kafkana.backend.services;

import com.kafkana.backend.abstraction.kafkaMonitorService;
import com.kafkana.backend.models.*;
import com.kafkana.backend.repositories.kafkaClusterRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Service
public class kafkaMonitorServiceImpl  implements kafkaMonitorService {
    @Autowired
    private kafkaClusterRepository kafkaClusterRepository;
    private static final Logger LOG = LoggerFactory.getLogger(kafkaMonitorServiceImpl.class);
    @Override
    public clusterSummaryModel getClusterSummary(Collection<topicModel> topics) {
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
        topicSummary.setPreferredReplicaPercent(topics.isEmpty() ? 0 : topicSummary.getPreferredReplicaPercent() / topics.size());
        topicSummary.setBrokerCount(topicSummary.getExpectedBrokerIds().size());
        topicSummary.setTimeStamp(new Date());
        return topicSummary;
    }
    @Override
    public List<topicModel> getTopics(String clusterId) {
        final  var kafkaConsumer= createConsumer(clusterId);
        final  var admin = getAdminClient(clusterId);
        final var topics = getTopicMetadata(clusterId,kafkaConsumer,admin).values().stream()
                .sorted(Comparator.comparing(topicModel::getName))
                .collect(Collectors.toList());
        kafkaConsumer.close();
        admin.close();
        return topics;
    }
    @Override
    public Optional<topicModel> getTopic(String topic,String clusterId) {
        final  var kafkaConsumer= createConsumer(clusterId);
        final  var admin = getAdminClient(clusterId);
        final var topicModel = Optional.ofNullable(getTopicMetadata(topic,kafkaConsumer,admin).get(topic));
        topicModel.ifPresent(model -> model.setPartitions(getTopicPartitionSizes(model,clusterId,kafkaConsumer)));
        kafkaConsumer.close();
        admin.close();
        return topicModel;
    }
    @Override
    public List<consumerModel> getConsumers(Collection<topicModel> topicModels,String clusterId) {
        final  var admin = getAdminClient(clusterId);
        final var topics = topicModels.stream().map(topicModel::getName).collect(Collectors.toSet());
        final var consumerGroupOffsets = getConsumerOffsets(topics,clusterId,admin);
        admin.close();
        return convert(consumerGroupOffsets, topicModels);
    }
    @Override
    public  List<messageModel> getMessages(String topic,String clusterId,int size){
        List<messageModel> messages = new ArrayList<>();
        Consumer<String, String> kafkaConsumer =this.createConsumer(clusterId);
        TopicPartition partition = new TopicPartition(topic, 0);


        kafkaConsumer.assign(Collections.singleton(partition));
        kafkaConsumer.seekToBeginning(Collections.singleton(partition));
        while (messages.size() < size){
            for (ConsumerRecord<String, String> record : kafkaConsumer.poll(Duration.ofMillis(200))) {
                if (messages.size() >= size) {
                    break;
                }
                messages.add(new messageModel(record.partition(),record.offset(),record.value(),record.key(),
                        headersToMap(record.headers())
                        ,new Date(record.timestamp())));

            }
        }
        return  messages;

    }

    @Override
    public  List<messageModel> getMessages(String topic,String clusterId,int size,long start, long end){
         List<messageModel> messages = new ArrayList<>();
        Consumer<String, String> kafkaConsumer =this.createConsumer(clusterId);
        TopicPartition partition = new TopicPartition(topic, 0);
        long beginningOffset = kafkaConsumer.offsetsForTimes(
                Collections.singletonMap(partition, start))
                .get(partition).offset();

        kafkaConsumer.assign(Collections.singleton(partition)); // must assign before seeking
        kafkaConsumer.seek(partition, beginningOffset);
       long lastMessageTimeStamp = start;
        while (lastMessageTimeStamp < end && messages.size() < size){
            for (ConsumerRecord<String, String> record : kafkaConsumer.poll(Duration.ofMillis(200))) {
                messages.add(new messageModel(record.partition(),record.offset(),record.value(),record.key(),
                        headersToMap(record.headers())
                        ,new Date(record.timestamp())));
                lastMessageTimeStamp = record.timestamp();
            }
        }

        return  messages;
    }

    @Override
    public  List<messageModel> getLatestMessages(String topic,String clusterId,int size){
        List<messageModel> messages = new ArrayList<>();
       final  var records = getLatestRecords(topic,size,clusterId);
       for(ConsumerRecord<String, String> record : records){
           messages.add(new messageModel(record.partition(),record.offset(),record.value(),record.key(),
                   headersToMap(record.headers())
                   ,new Date(record.timestamp())));
       }
       return  messages;
    }


    private  List<ConsumerRecord<String, String>> getLatestRecords(String topic,
                                                                   int count,String clusterId) {
        Consumer<String, String> kafkaConsumer =this.createConsumer(clusterId);

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

        final var totalCount = count * partitions.size();
        final Map<TopicPartition, List<ConsumerRecord<String, String>>> rawRecords
                = partitions.stream().collect(Collectors.toMap(p -> p , p -> new ArrayList<>(count)));

        var moreRecords = true;
        while (rawRecords.size() < totalCount && moreRecords) {
            final var polled = kafkaConsumer.poll(Duration.ofMillis(200));

            moreRecords = false;
            for (var partition : polled.partitions()) {
                var records = polled.records(partition);
                if (!records.isEmpty()) {
                    rawRecords.get(partition).addAll(records);
                    moreRecords = records.get(records.size() - 1).offset() < latestOffsets.get(partition) - 1;
                }
            }
        }

        return rawRecords
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Map<String, topicModel> getTopicMetadata(String cluserId, Consumer<String,String> consumer,AdminClient adminClient,String... topics) {
        final var topicsMap = getTopicInformation(topics,cluserId,consumer);
        final var retrievedTopicNames = topicsMap.keySet();
        Map<String, Config> topicConfigs = new HashMap<>();
        try {
            topicConfigs = describeTopicConfigs(retrievedTopicNames,cluserId,adminClient);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (var topicModel : topicsMap.values()) {
            final var config = topicConfigs.get(topicModel.getName());
            if (config != null) {
                final var configMap = new TreeMap<String, String>();
                for (var configEntry : config.entries()) {
                    if (configEntry.source() != ConfigEntry.ConfigSource.DEFAULT_CONFIG &&
                            configEntry.source() != ConfigEntry.ConfigSource.STATIC_BROKER_CONFIG) {
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

    private Map<Integer, topicPartitionModel> getTopicPartitionSizes(topicModel topic,String clusterId,Consumer<String,String> consumer) {
        return getPartitionSize(topic.getName(),clusterId,consumer);
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

        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,
                30000);
        props.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG,
                30000);


        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        final Consumer<String, String> consumer =
                new KafkaConsumer<>(props);
        return consumer;
    }

    private AdminClient getAdminClient(String clusterId) {
        Optional<kafkaCluster> cluster = this.kafkaClusterRepository.getById(clusterId);
        if(cluster.isEmpty()){
            throw new NullPointerException("Cluster not found");
        }
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, cluster.get().getBootStrapServers());
        config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        return AdminClient.create(config);
    }

    synchronized Map<Integer, topicPartitionModel> getPartitionSize(String topic,String clusterId,Consumer<String,String> kafkaConsumer) {

        final var partitionInfoSet = kafkaConsumer.partitionsFor(topic);
        kafkaConsumer.assign(partitionInfoSet.stream()
                .map(partitionInfo -> new TopicPartition(partitionInfo.topic(),
                        partitionInfo.partition()))
                .collect(Collectors.toList()));

        kafkaConsumer.poll(Duration.ofMillis(0));
        final Set<TopicPartition> assignedPartitionList = kafkaConsumer.assignment();
        final topicModel topicVO = getTopicInfo(topic,clusterId,kafkaConsumer);
        final Map<Integer, topicPartitionModel> partitionMap = topicVO.getPartitionMap();

        kafkaConsumer.seekToBeginning(assignedPartitionList);
        assignedPartitionList.forEach(topicPartition -> {
            final topicPartitionModel topicPartitionVo = partitionMap.get(topicPartition.partition());
            final long startOffset = kafkaConsumer.position(topicPartition);
            LOG.debug("topic: {}, partition: {}, startOffset: {}", topicPartition.topic(), topicPartition.partition(), startOffset);
            topicPartitionVo.setFirstOffset(startOffset);
        });

        kafkaConsumer.seekToEnd(assignedPartitionList);
        assignedPartitionList.forEach(topicPartition -> {
            final long latestOffset = kafkaConsumer.position(topicPartition);
            LOG.debug("topic: {}, partition: {}, latestOffset: {}", topicPartition.topic(), topicPartition.partition(), latestOffset);
            final topicPartitionModel partitionVo = partitionMap.get(topicPartition.partition());
            partitionVo.setSize(latestOffset);
        });


        return partitionMap;
    }

    private topicModel getTopicInfo(String topic,String clusterId,Consumer<String,String> kafkaConsumer) {
        final var partitionInfoList = kafkaConsumer.partitionsFor(topic);
        final var topicModel = new topicModel(topic);
        final var partitions = new TreeMap<Integer, topicPartitionModel>();

        for (var partitionInfo : partitionInfoList) {
            final var topicPartitionVo = new topicPartitionModel(partitionInfo.partition());
            final var inSyncReplicaIds = Arrays.stream(partitionInfo.inSyncReplicas()).map(Node::id).collect(Collectors.toSet());
            final var offlineReplicaIds = Arrays.stream(partitionInfo.offlineReplicas()).map(Node::id).collect(Collectors.toSet());

            for (var node : partitionInfo.replicas()) {
                final var isInSync = inSyncReplicaIds.contains(node.id());
                final var isOffline = offlineReplicaIds.contains(node.id());
                topicPartitionVo.addReplica(new topicPartitionModel.PartitionReplica(node.id(), isInSync, false, isOffline));
            }

            final var leader = partitionInfo.leader();
            if (leader != null) {
                topicPartitionVo.addReplica(new topicPartitionModel.PartitionReplica(leader.id(), true, true, false));
            }
            partitions.put(partitionInfo.partition(), topicPartitionVo);
        }

        topicModel.setPartitions(partitions);

        return topicModel;
    }

    synchronized Map<String, topicModel> getTopicInformation(String[] topics, String clusterId, Consumer<String,String> kafkaConsumer ) {
        final var topicSet = kafkaConsumer.listTopics().keySet();

        if (topics.length == 0) {
            topics = Arrays.copyOf(topicSet.toArray(), topicSet.size(), String[].class);
        }
        final var topicModelMap = new HashMap<String, topicModel>(topics.length, 1f);

        for (var topic : topics) {
            if (topicSet.contains(topic)) {
                topicModelMap.put(topic, getTopicInfo(topic,clusterId,kafkaConsumer));
            }
        }
        return topicModelMap;
    }

    Map<String, Config> describeTopicConfigs(Set<String> topicNames,String clusterId,AdminClient adminClient )  {
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

            }
            throw new RuntimeException(e);
        }

        return configsByTopic;
    }

    Set<String> listConsumerGroups(String clusterId,AdminClient adminClient) {
        final Collection<ConsumerGroupListing> groupListing;
        try {
            groupListing = adminClient.listConsumerGroups().all().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


        return groupListing.stream().map(ConsumerGroupListing::groupId).collect(Collectors.toSet());
    }

    private ConsumerGroupOffsets resolveOffsets(String groupId,String clusterId,AdminClient adminClient) {
        return new ConsumerGroupOffsets(groupId,listConsumerGroupOffsetsIfAuthorized(groupId,clusterId,adminClient));
    }

    Map<TopicPartition, OffsetAndMetadata> listConsumerGroupOffsetsIfAuthorized(String groupId,String clusterId,AdminClient adminClient) {
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

    private List<ConsumerGroupOffsets> getConsumerOffsets(Set<String> topics,String clusterId,AdminClient adminClient) {
        final var consumerGroups = listConsumerGroups(clusterId,adminClient);
        return consumerGroups.stream()
                .map(c-> {
                    return  resolveOffsets(c,clusterId,adminClient);
                })
                .map(offsets -> offsets.forTopics(topics))
                .filter(not(ConsumerGroupOffsets::isEmpty))
                .collect(Collectors.toList());
    }

    private static final class ConsumerGroupOffsets {
        final String groupId;
        final Map<TopicPartition, OffsetAndMetadata> offsets;

        ConsumerGroupOffsets(String groupId, Map<TopicPartition, OffsetAndMetadata> offsets) {
            this.groupId = groupId;
            this.offsets = offsets;
        }

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

    private static List<consumerModel> convert(List<ConsumerGroupOffsets> consumerGroupOffsets, Collection<topicModel> topicVos) {
        final var topicMap = topicVos.stream().collect(Collectors.toMap(topicModel::getName, Function.identity()));
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
            final var consumerVo = new consumerModel(groupId);
            consumerModels.add(consumerVo);

            for (var topicPartitionOffset : groupTopicPartitionOffset.getValue().entrySet()) {
                final var topic = topicPartitionOffset.getKey();
                final var consumerTopicVo = new consumerTopicModel(topic);
                consumerVo.addTopic(consumerTopicVo);

                for (var partitionOffset : topicPartitionOffset.getValue().entrySet()) {
                    final var partition = partitionOffset.getKey();
                    final var offset = partitionOffset.getValue();
                    final var offsetVo = new consumerPartitionModel(groupId, topic, partition);
                    consumerTopicVo.addOffset(offsetVo);
                    offsetVo.setOffset(offset);
                    final var topicVo = topicMap.get(topic);
                    final var topicPartitionVo = topicVo.getPartition(partition);
                    offsetVo.setSize(topicPartitionVo.map(topicPartitionModel::getSize).orElse(-1L));
                    offsetVo.setFirstOffset(topicPartitionVo.map(topicPartitionModel::getFirstOffset).orElse(-1L));
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

}
