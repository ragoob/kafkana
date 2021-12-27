package com.kafkana.backend.services;

import com.kafkana.backend.abstraction.kafkaMonitorService;
import com.kafkana.backend.configurations.AppConfig;
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
import org.glassfish.jersey.internal.util.Property;
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
    private AppConfig appConfig;
    private static final Logger LOG = LoggerFactory.getLogger(kafkaMonitorServiceImpl.class);
    @Override
    public clusterSummaryModel getClusterSummary(String clusterIp) {
        Collection<topicModel> topics = this.getTopicsWithDetails(clusterIp,false);
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
    public List<topicModel> getTopics(String clusterIp, boolean showDefaultConfig) {
        final var admin = getAdminClient(clusterIp);
        try{
            return admin.listTopics()
                    .names().get().stream()
                    .map(c-> new topicModel(c)).toList();
        } catch (Exception ex){
            admin.close();
            admin.close();
            return  new ArrayList<>();
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
    public List<consumerModel> getConsumers(String clusterIp) {
        final  var admin = getAdminClient(clusterIp);
        final  var consumer  =this.createConsumer(clusterIp);
        List<consumerModel> consumerList = new ArrayList<>();
        try{
            List<String> groupIds = admin.listConsumerGroups().all().get().
                    stream().map(s -> s.groupId()).collect(Collectors.toList());
            Map<String, ConsumerGroupDescription> groups = admin.
                    describeConsumerGroups(groupIds).all().get();
            for (final String groupId : groupIds) {
                Map<TopicPartition, OffsetAndMetadata> groupOffsets =
                        admin.listConsumerGroupOffsets(groupId)
                                .partitionsToOffsetAndMetadata().get();

                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(groupOffsets.keySet());


                ConsumerGroupDescription description = groups.get(groupId);
                var coordinator = description.coordinator();
                var brokers = new brokers(coordinator.id(), coordinator.host(), coordinator.port(), coordinator.rack());
                var group = new consumerModel(description.groupId(), brokers);
                var members = new ArrayList<consumerMemberModel>();
                description.members().forEach(c -> {
                    c.assignment().topicPartitions().forEach(t-> {
                        var member = new consumerMemberModel();
                        member.setId(c.consumerId());
                        member.setClientId(c.clientId());
                        member.setHost(c.host().replace("/",""));
                        member.setTopic(t.topic());
                        member.setPartition(t.partition());
                        var topicOffset = groupOffsets.get(new TopicPartition(t.topic(),t.partition()));
                        var endOffset = endOffsets.get(new TopicPartition(t.topic(),t.partition()));
                        if(topicOffset != null){
                            member.setLastCommittedOffset(topicOffset.offset());
                        }
                        if(endOffset != null){
                            member.setEndOffsets(endOffset);
                        }
                        member.setLag(member.getEndOffsets() - member.getLastCommittedOffset());
                        members.add(member);
                    });

                });
                group.setMembers(members);
                consumerList.add(group);
            }
        }catch (Exception ex){
            System.out.println(ex);
        }

        admin.close();
        consumer.close();
        return  consumerList;
    }



    @Override
    public  List<messageModel> getMessages(String topic,String clusterIp,long size,long start, long end,String sortingDirection){
        final var records = getLatestRecords(topic, size,clusterIp,start,end);
        if (records != null  && records.size() > 0) {
            final var messages = new ArrayList<messageModel>();
            for (var record : records) {
                final var message = new messageModel();
                message.setPartition(record.partition());
                message.setOffset(record.offset());
                message.setKey(record.key());
                message.setMessage(record.value());
                message.setHeaders(headersToMap(record.headers()));
                message.setTimestamp(new Date(record.timestamp()));
                messages.add(message);
            }
            return sortingDirection.equals(pollingTypes.DESC) ?  messages.stream().sorted((f1, f2) -> Long.compare(f2.getTimestamp().getTime(), f1.getTimestamp().getTime()))
                    .collect(Collectors.toList()) : messages;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public  List<messageModel> getMessages(String topic,String clusterIp,long size,long start, long end,String sortingDirection,int partition){
        final var records = getLatestRecords(topic, size,clusterIp,start,end,partition);

        if (records != null  && records.size() > 0) {
            final var messages = new ArrayList<messageModel>();
            for (var record : records) {
                final var message = new messageModel();
                message.setPartition(record.partition());
                message.setOffset(record.offset());
                message.setKey(record.key());
                message.setMessage(record.value());
                message.setHeaders(headersToMap(record.headers()));
                message.setTimestamp(new Date(record.timestamp()));
                messages.add(message);
            }
            return sortingDirection.equals(pollingTypes.DESC) ?  messages.stream().sorted((f1, f2) -> Long.compare(f2.getTimestamp().getTime(), f1.getTimestamp().getTime()))
                    .collect(Collectors.toList()) : messages;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public  List<messageModel> getMessages(String topic,String clusterIp,long size,long start,String sortingDirection){
        final var records = getLatestRecords(topic, size,clusterIp,start);
        if (records != null && records.size() > 0) {
            final var messages = new ArrayList<messageModel>();
            for (var record : records) {
                final var message = new messageModel();
                message.setPartition(record.partition());
                message.setOffset(record.offset());
                message.setKey(record.key());
                message.setMessage(record.value());
                message.setHeaders(headersToMap(record.headers()));
                message.setTimestamp(new Date(record.timestamp()));
                messages.add(message);
            }
            return sortingDirection.equals(pollingTypes.DESC) ?  messages.stream().sorted((f1, f2) -> Long.compare(f2.getTimestamp().getTime(), f1.getTimestamp().getTime()))
                    .collect(Collectors.toList()) : messages;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public  List<messageModel> getMessagesUntilTime(String topic,String clusterIp,long size,long end,String sortingDirection){
        final var records = getLatestRecordsUntilTime(topic, size,clusterIp,end);
        if (records != null &&  records.size() > 0) {
            final var messages = new ArrayList<messageModel>();
            for (var record : records) {
                final var message = new messageModel();
                message.setPartition(record.partition());
                message.setOffset(record.offset());
                message.setKey(record.key());
                message.setMessage(record.value());
                message.setHeaders(headersToMap(record.headers()));
                message.setTimestamp(new Date(record.timestamp()));
                messages.add(message);
            }
            return sortingDirection.equals(pollingTypes.DESC) ?  messages.stream().sorted((f1, f2) -> Long.compare(f2.getTimestamp().getTime(), f1.getTimestamp().getTime()))
                    .collect(Collectors.toList()) : messages;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public  List<messageModel> getMessages(String topic,String clusterIp,long size,String sortingDirection){
        final var records = getLatestRecords(topic, size,clusterIp,sortingDirection);
        if (records != null && records.size() > 0) {
            final var messages = new ArrayList<messageModel>();
            for (var record : records) {
                final var message = new messageModel();
                message.setPartition(record.partition());
                message.setOffset(record.offset());
                message.setKey(record.key());
                message.setMessage(record.value());
                message.setHeaders(headersToMap(record.headers()));
                message.setTimestamp(new Date(record.timestamp()));
                messages.add(message);
            }
            return sortingDirection.equals(pollingTypes.DESC) ?  messages.stream().sorted((f1, f2) -> Long.compare(f2.getTimestamp().getTime(), f1.getTimestamp().getTime()))
                    .collect(Collectors.toList()) : messages;
        } else {
            return Collections.emptyList();
        }
    }



    @Override
    public Map<Integer,Long> getLastOffsetPerPartition(String topic, String clusterIp) {
        Consumer<String, String> kafkaConsumer =this.createConsumer(clusterIp);
        final var partitionInfoSet = kafkaConsumer.partitionsFor(topic);
        final var partitions = partitionInfoSet.stream()
                .map(partitionInfo -> new TopicPartition(partitionInfo.topic(),
                        partitionInfo.partition()))
                .collect(Collectors.toList());
        kafkaConsumer.assign(partitions);
        final var latestOffsets = kafkaConsumer.endOffsets(partitions);
        Map<Integer,Long> map = new HashMap<>();
        for (var partition : partitions) {
            map.put(partition.partition(), latestOffsets.get(partition));
        }
        kafkaConsumer.close();
        return  map;
    }


    @Override
    public Map<String, Map<String, Long>> GetLastOffsetPerPartition(String clusterIp) {
        Consumer<String, String> kafkaConsumer =this.createConsumer(clusterIp);
        Map<String,Map<String,Long>> map = new HashMap<>();
       final var allTopics = this.getTopics(clusterIp,false);
        allTopics.forEach(topic-> {
           final var partitionInfoSet = kafkaConsumer.partitionsFor(topic.getName());
           final var partitions = partitionInfoSet.stream()
                   .map(partitionInfo -> new TopicPartition(partitionInfo.topic(),
                           partitionInfo.partition()))
                   .collect(Collectors.toList());
           kafkaConsumer.assign(partitions);
           final var latestOffsets = kafkaConsumer.endOffsets(partitions);
           Map<String,Long> offsetMap = new HashMap<>();
           for (var partition : partitions) {
               offsetMap.put(Integer.toString(partition.partition()), (latestOffsets.get(partition) - 1));
           }
           map.put(topic.getName(),offsetMap);
       });
        kafkaConsumer.close();
       return  map;
    }




    private  List<ConsumerRecord<String, String>> getLatestRecords(String topic,
                                                                   long count,String clusterIp, String sortingDirection) {
        Consumer<String, String> kafkaConsumer =this.createConsumer(clusterIp);
        final var partitionInfoSet = kafkaConsumer.partitionsFor(topic);
        final var partitions = partitionInfoSet.stream()
                .map(partitionInfo -> new TopicPartition(partitionInfo.topic(),
                        partitionInfo.partition()))
                .collect(Collectors.toList());
        long totalOffsetsCounts = 0;
           try{
               kafkaConsumer.assign(partitions);
               final var latestOffsets = kafkaConsumer.endOffsets(partitions);

               for (var partition : partitions) {
                   final var latestOffset = Math.max(0, latestOffsets.get(partition));
                   totalOffsetsCounts = totalOffsetsCounts + latestOffset;
                   if(sortingDirection.equals(pollingTypes.DESC) &&  latestOffset > count){
                       long startFrom =   latestOffset - count;
                       kafkaConsumer.seek(partition, Math.max(0, startFrom));
                   }
                   else{
                       kafkaConsumer.seek(partition,0);
                   }
               }
           }catch (Exception ex){
               System.out.println("Error in assign partitions " + ex.getMessage());
           }

        List<ConsumerRecord<String, String>> messages = new ArrayList<>();
        if(totalOffsetsCounts == 0)
        {
            kafkaConsumer.close();
            return messages;
        }
        if(totalOffsetsCounts < count){
            count =  totalOffsetsCounts;
        }
        boolean moreRecords = true;
        int polledOffsets = 0;
        int emptyPollingTimes = 0;
        while (moreRecords) {
            if(moreRecords){
                if(emptyPollingTimes == partitions.size()) {
                    moreRecords = false;
                    return messages;
                }
                System.out.println("********* Start fetching records from " + topic + " *********");
            final var polled = kafkaConsumer.poll(Duration.ofMillis(appConfig.getKafka().getPollduration()));
                System.out.println("********* polled records count in " + topic + " " + polled.count() + " *********");
                if(polled.count() == 0) {
                    emptyPollingTimes++;
                }
                var records = polled.records(topic);

                polledOffsets = polledOffsets +  polled.count();
                moreRecords = polledOffsets < count;
                for(var record : records){
                    if(messages.size() < count)
                        messages.add(record);
                    else
                    {
                        moreRecords = false;
                        break;
                    }

                }

            }
            }

        kafkaConsumer.close();
        return  messages;
    }

    private  List<ConsumerRecord<String, String>> getLatestRecords(String topic,
                                                                   long count,String clusterIp,long from,long to) {
        Consumer<String, String> kafkaConsumer =this.createConsumer(clusterIp);
        final var partitionInfoSet = kafkaConsumer.partitionsFor(topic);
        final var partitions = partitionInfoSet.stream()
                .map(partitionInfo -> new TopicPartition(partitionInfo.topic(),
                        partitionInfo.partition()))
                .collect(Collectors.toList());
        kafkaConsumer.assign(partitions);
        final var latestOffsets = kafkaConsumer.endOffsets(partitions);
        long totalOffsetsCounts = 0;
        short seekError = 0;
        for (var partition : partitions) {
            final var latestOffset = Math.max(0, latestOffsets.get(partition));
            totalOffsetsCounts = totalOffsetsCounts + latestOffset;
            var timStampMap = new HashMap<TopicPartition,Long>();
            timStampMap.put(partition,from);
            var timeStampOffset  = kafkaConsumer.offsetsForTimes(timStampMap);
            try{
                kafkaConsumer.seek(partition, timeStampOffset.get(partition).offset());
            }catch (Exception ex){
                seekError++;
                if(seekError == partitions.size()){
                    kafkaConsumer.close();
                    return  new ArrayList<>();
                }
            }

        }

        List<ConsumerRecord<String, String>> messages = new ArrayList<>();
        if(totalOffsetsCounts == 0){
            kafkaConsumer.close();
            return  messages;
        }
        boolean moreRecords = true;
        int polledOffsets = 0;
        int emptyPollingTimes =0;
        while (moreRecords) {
            if(moreRecords){
                if(emptyPollingTimes == 3) {
                    moreRecords = false;
                    return messages;
                }
                final var polled = kafkaConsumer.poll(Duration.ofMillis(appConfig.getKafka().getPollduration()));
                if(polled.count() == 0){
                    emptyPollingTimes++;
                }
                var records = polled.records(topic);
                polledOffsets = polledOffsets +  polled.count();
                moreRecords = polledOffsets < count && totalOffsetsCounts >= count;
                for (var record: records){
                    if(messages.size() < count && record.timestamp() <= to)
                        messages.add(record);
                    else
                    {
                        moreRecords = false;
                        break;
                    }
                }

            }
        }
        kafkaConsumer.close();
        return  messages;
    }


    private  List<ConsumerRecord<String, String>> getLatestRecords(String topic,
                                                                   long count,String clusterIp,long from,long to, int partition) {
        Consumer<String, String> kafkaConsumer =this.createConsumer(clusterIp);
        kafkaConsumer.assign(Collections.singleton(new TopicPartition(topic,partition)));
        try{
            kafkaConsumer.seek(new TopicPartition(topic,partition),from);
        }catch (Exception ex){
            System.out.println("err in seek " + ex.getMessage());
            return  new ArrayList<>();
        }
        List<ConsumerRecord<String, String>> messages = new ArrayList<>();
        if(from == 0){
            kafkaConsumer.close();
            return  messages;
        }
        boolean moreRecords = true;
        int polledOffsets = 0;
        int emptyPollingTimes =0;
        while (moreRecords) {
            if(moreRecords){
                if(emptyPollingTimes == 3) {
                    moreRecords = false;
                    return messages;
                }
                final var polled = kafkaConsumer.poll(Duration.ofMillis(appConfig.getKafka().getPollduration()));
                if(polled.count() == 0){
                    emptyPollingTimes++;
                }
                var records = polled.records(topic);
                polledOffsets = polledOffsets +  polled.count();
                moreRecords = polledOffsets < count;
                for (var record: records){
                    if(messages.size() < count && (record.offset() <= to || to == 0))
                        messages.add(record);
                    else
                    {
                        moreRecords = false;
                        break;
                    }
                }

            }
        }
        kafkaConsumer.close();
        return  messages;
    }

    private  List<ConsumerRecord<String, String>> getLatestRecords(String topic,
                                                                   long count,String clusterIp,long from) {
        Consumer<String, String> kafkaConsumer =this.createConsumer(clusterIp);
        final var partitionInfoSet = kafkaConsumer.partitionsFor(topic);
        final var partitions = partitionInfoSet.stream()
                .map(partitionInfo -> new TopicPartition(partitionInfo.topic(),
                        partitionInfo.partition()))
                .collect(Collectors.toList());
        kafkaConsumer.assign(partitions);
        final var latestOffsets = kafkaConsumer.endOffsets(partitions);
        long totalOffsetsCounts = 0;
        short seekError = 0;
        for (var partition : partitions) {
            final var latestOffset = Math.max(0, latestOffsets.get(partition));
            totalOffsetsCounts = totalOffsetsCounts + latestOffset;
            var timStampMap = new HashMap<TopicPartition,Long>();
            timStampMap.put(partition,from);
            var timeStampOffset  = kafkaConsumer.offsetsForTimes(timStampMap);
            try{
                kafkaConsumer.seek(partition, timeStampOffset.get(partition).offset());
            }catch (Exception ex){
                seekError++;
                if(seekError == partitions.size()){
                    kafkaConsumer.close();
                    return  new ArrayList<>();
                }
            }
        }

        List<ConsumerRecord<String, String>> messages = new ArrayList<>();
        if(totalOffsetsCounts == 0)
        {
            kafkaConsumer.close();
            return  messages;
        }
        boolean moreRecords = true;
        int polledOffsets = 0;
        int emptyPollingTimes =0;
        while (moreRecords) {
            if(moreRecords){
                if(emptyPollingTimes == partitions.size()) {
                    moreRecords = false;
                    return messages;
                }
                final var polled = kafkaConsumer.poll(Duration.ofMillis(appConfig.getKafka().getPollduration()));
                if(polled.count() == 0){
                    emptyPollingTimes++;
                }
                var records = polled.records(topic);
                polledOffsets = polledOffsets +  polled.count();
                moreRecords = polledOffsets < count && totalOffsetsCounts >= count;
                for (var record: records){
                    if(messages.size() < count)
                        messages.add(record);
                    else
                    {
                        moreRecords = false;
                        break;
                    }
                }

            }
        }
        kafkaConsumer.close();
        return  messages;
    }


    private  List<ConsumerRecord<String, String>> getLatestRecordsUntilTime(String topic,
                                                                   long count,String clusterIp,long to) {
        Consumer<String, String> kafkaConsumer =this.createConsumer(clusterIp);
        final var partitionInfoSet = kafkaConsumer.partitionsFor(topic);
        final var partitions = partitionInfoSet.stream()
                .map(partitionInfo -> new TopicPartition(partitionInfo.topic(),
                        partitionInfo.partition()))
                .collect(Collectors.toList());
        kafkaConsumer.assign(partitions);
        final var latestOffsets = kafkaConsumer.endOffsets(partitions);
        long totalOffsetsCounts = 0;
        for (var partition : partitions) {
            final var latestOffset = Math.max(0, latestOffsets.get(partition));
            totalOffsetsCounts = totalOffsetsCounts + latestOffset;
            kafkaConsumer.seek(partition, 0);
        }

        List<ConsumerRecord<String, String>> messages = new ArrayList<>();
        if(totalOffsetsCounts == 0)
        {
            kafkaConsumer.close();
            return  messages;
        }
        boolean moreRecords = true;
        int polledOffsets = 0;
        int emptyPollingTimes = 0;
        while (moreRecords) {
            if(moreRecords){
                if(emptyPollingTimes == partitions.size()) {
                    moreRecords = false;
                    return messages;
                }
                final var polled = kafkaConsumer.poll(Duration.ofMillis(appConfig.getKafka().getPollduration()));
                if(polled.count() == 0){
                    emptyPollingTimes++;
                }
                var records = polled.records(topic);
                polledOffsets = polledOffsets +  polled.count();
                moreRecords = polledOffsets < count && totalOffsetsCounts >= count;
                for (var record: records){
                    if(messages.size() < count && record.timestamp() <= to)
                        messages.add(record);
                    else
                       {
                           moreRecords = false;
                           break;
                       }
                }

            }
        }
        kafkaConsumer.close();
        return  messages;
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
        props.put(ConsumerConfig.GROUP_ID_CONFIG,appConfig.getKafka().getConsumergroup()
                );
        props.put(ConsumerConfig.CLIENT_ID_CONFIG,
                "KAFKANA_UI_MONITORING-CONUMER_" + UUID.randomUUID().toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
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
            topicPartitionModel.setFirstOffset(startOffset);
        });

        kafkaConsumer.seekToEnd(assignedPartitionList);
        assignedPartitionList.forEach(topicPartition -> {
            final long latestOffset = kafkaConsumer.position(topicPartition);
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

    private List<topicModel> getTopicsWithDetails(String clusterIp, boolean showDefaultConfig){
        final  var kafkaConsumer= createConsumer(clusterIp);
        final  var admin = getAdminClient(clusterIp);
        try{

            final var topics = getTopicMetadata(kafkaConsumer,admin,showDefaultConfig).values().stream()
                    .sorted(Comparator.comparing(topicModel::getName))
                    .collect(Collectors.toList());
            topics.forEach(topic-> {
                topic.setPartitions(getTopicPartitionSizes(topic,kafkaConsumer));
            });
            kafkaConsumer.close();
            admin.close();
            return topics;
        } catch (Exception ex){
            kafkaConsumer.close();
            admin.close();
            throw ex;
        }
    }
}
