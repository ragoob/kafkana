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
                                          ) throws InterruptedException {
        boolean refreshFlag = refresh != null ? refresh : false;
        clusterSummaryModel summaryModel;
        if(refreshFlag || !allowCache){
            summaryModel  = this.kafkaMonitorService.getClusterSummary(clusterIp);
            if(allowCache)
            this.summaryRepository.save(summaryModel,clusterIp);
        }
        else{
            var cached = this.summaryRepository.find(clusterIp);
            if(cached != null && cached.getBrokerCount() > 0) {
                summaryModel = cached;
            }
            else{
                summaryModel =  this.summaryRepository.find(clusterIp);
                this.summaryRepository.save(summaryModel,clusterIp);
            }

        }

        return summaryModel;


    }

    @GetMapping("/topics")
    List<topicModel> getTopics(@RequestHeader(value = "clusterIp") String clusterIp,
                               @RequestParam(name = "refresh", required = false) Boolean  refresh
                               ){
        boolean refreshFlag = refresh != null ? refresh : false;
        List<topicModel> topics;
        if(refreshFlag || !allowCache){
            topics =  this.kafkaMonitorService.getTopics(clusterIp,false);
            if(allowCache)
            this.topicsRepository.save(topics,clusterIp);
        }
        else{
            var cached = this.topicsRepository.findAll(clusterIp);
            if(cached.size() > 0)
                topics = cached;
            else{
                topics = this.kafkaMonitorService.getTopics(clusterIp,false);
                this.topicsRepository.save(topics,clusterIp);
            }

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
        if(refreshFlag || !allowCache){
            final  var topics = this.kafkaMonitorService.getTopics(clusterIp,false);
            consumers =    this.kafkaMonitorService.getConsumers(topics,clusterIp);
            if(allowCache)
            this.consumerRepository.save(consumers,clusterIp);
        }
        else{

            var cached = this.consumerRepository.findAll(clusterIp);
            if(cached.size() > 0){
                consumers = cached;
            }
            else{
                final  var topics = this.kafkaMonitorService.getTopics(clusterIp,false);
                consumers =   this.kafkaMonitorService.getConsumers(topics,clusterIp);;
                this.consumerRepository.save(consumers,clusterIp);
            }

        }

        return  consumers;
    }

    @GetMapping("/messages/{name:.+}")
    List<messageModel> getMessages(@RequestHeader(value = "clusterIp") String clusterIp, @PathVariable(value = "name") String name,
                                   @RequestParam(name = "size", required = false) Integer size,
                                   @RequestParam(name = "start", required = false) Long  start,
                                   @RequestParam(name = "end", required = false) Long  end,
                                   @RequestParam(name = "sortDirection", required = false) String  sortDirection
                                   ){
        final long count = (size != null? size : 200);
        final String sortDir = sortDirection != null ? sortDirection : pollingTypes.ASC;
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
