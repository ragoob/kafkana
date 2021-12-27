package com.kafkana.backend.controllers;
import com.kafkana.backend.abstraction.*;
import com.kafkana.backend.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController()
@CrossOrigin
@RequestMapping("/api/monitoring")
public class kafkaMonitoringController {
    @Autowired
    private kafkaMonitorService kafkaMonitorService ;

    @Autowired
    private kafkaAdminService kafkaAdminService;

    @Autowired(required = false)
    private topicsRepository topicsRepository;

    @Autowired(required = false)
    private summaryRepository summaryRepository;

    @Autowired(required = false)
    private consumerRepository consumerRepository;

    @Value("${cache.allowed}")
    private  boolean allowCache;

    @GetMapping("/summary")
    clusterSummaryModel getClusterSummary(@RequestHeader("clusterIp") String clusterIp,
                                          @RequestParam(name = "refresh", required = false) Boolean  refresh
                                          ) {
        boolean refreshFlag = refresh != null ? refresh : false;
        clusterSummaryModel summaryModel;
        if(allowCache){
            var cached = this.summaryRepository.find(clusterIp);
            summaryModel = cached == null || refreshFlag ? this.kafkaMonitorService.getClusterSummary(clusterIp) : cached;
            this.summaryRepository.save(summaryModel,clusterIp);
        }else{
            summaryModel  = this.kafkaMonitorService.getClusterSummary(clusterIp);
        }

        return summaryModel;


    }

    @GetMapping("/topics")
    List<topicModel> getTopics(@RequestHeader(value = "clusterIp") String clusterIp,
                               @RequestParam(name = "refresh", required = false) Boolean  refresh
                               ){
        boolean refreshFlag = refresh != null ? refresh : false;
        List<topicModel> topics;
        if(allowCache){
            var cached = this.kafkaMonitorService.getTopics(clusterIp,false);
            topics = cached.size() == 0 || refreshFlag ? this.kafkaMonitorService.getTopics(clusterIp,false) : cached;
            this.topicsRepository.save(topics,clusterIp);

        }else{
            topics = this.kafkaMonitorService.getTopics(clusterIp,false);
        }

       return  topics;

    }

    @GetMapping("/topics/{name:.+}")
    Optional<topicModel> getTopic(@RequestHeader(value = "clusterIp") String clusterIp, @PathVariable(value = "name") String name, @RequestParam(value = "showDefaultConfig",required = false) Boolean showDefaultConfig){
        final boolean ShowDefaultConfigFlag = (showDefaultConfig != null? showDefaultConfig : false);

        return this.kafkaMonitorService.getTopic(name,clusterIp,ShowDefaultConfigFlag);
    }

    @GetMapping("/consumers")
    List<consumerModel> getConsumers(@RequestHeader(value = "clusterIp") String clusterIp,
                                     @RequestParam(name = "refresh", required = false) Boolean  refresh
                                     ){
        boolean refreshFlag = refresh != null ? refresh : false;
        List<consumerModel> consumers;
        if(allowCache){
            var cached = this.consumerRepository.findAll(clusterIp);
            consumers = cached.size() == 0 || refreshFlag ? this.kafkaMonitorService.getConsumers(clusterIp) : cached;
            this.consumerRepository.save(consumers,clusterIp);
        }else{
            consumers = this.kafkaMonitorService.getConsumers(clusterIp);
        }
        return  consumers;
    }

    @GetMapping("/messages/{name:.+}")
    List<messageModel> getMessages(@RequestHeader(value = "clusterIp") String clusterIp, @PathVariable(value = "name") String name,
                                   @RequestParam(name = "size", required = false) Integer size,
                                   @RequestParam(name = "start", required = false) Long  start,
                                   @RequestParam(name = "end", required = false) Long  end,
                                   @RequestParam(name = "partition",required = false) Integer partition,
                                   @RequestParam(name = "sortDirection", required = false) String  sortDirection
                                   ){
        final long count = (size != null? size : 200);
        final String sortDir = sortDirection != null ? sortDirection : pollingTypes.ASC;
        if(partition != null){

            return this.kafkaMonitorService.getMessages(name,clusterIp,count,start,end,sortDir,partition);
        }
        if(start == null && end == null){
            return this.kafkaMonitorService.getMessages(name,clusterIp,count,sortDir);
        }
        else if(end == null){
            return this.kafkaMonitorService.getMessages(name,clusterIp,count,start,sortDir);
        }
        else if(start == null){
            return this.kafkaMonitorService.getMessagesUntilTime(name,clusterIp,count,end,sortDir);
        }
        else{
            return this.kafkaMonitorService.getMessages(name,clusterIp,count,start,end,sortDir);
        }
    }

    @GetMapping("/lastOffsets/{name:.+}")
    Map<Integer,Long> getLastOffsets(@RequestHeader(value = "clusterIp") String clusterIp, @PathVariable(value = "name") String name
    ){
        return this.kafkaMonitorService.getLastOffsetPerPartition(name,clusterIp);
    }

    @GetMapping("/lastOffsets")
    Map<String,Map<String,Long>> getLastOffsets(@RequestHeader(value = "clusterIp") String clusterIp
    ){
        return this.kafkaMonitorService.GetLastOffsetPerPartition(clusterIp);
    }

}
