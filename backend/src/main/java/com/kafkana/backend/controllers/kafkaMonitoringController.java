package com.kafkana.backend.controllers;

import com.kafkana.backend.abstraction.kafkaAdminService;
import com.kafkana.backend.abstraction.kafkaMonitorService;
import com.kafkana.backend.models.clusterSummaryModel;
import com.kafkana.backend.models.consumerModel;
import com.kafkana.backend.models.messageModel;
import com.kafkana.backend.models.topicModel;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.PathParam;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@RestController()
@CrossOrigin
@RequestMapping("/api/monitoring")
public class kafkaMonitoringController {
    @Autowired
    private kafkaMonitorService kafkaMonitorService ;

    @Autowired
    private kafkaAdminService kafkaAdminService;

    @Autowired
    CacheManager cacheManager;

    @GetMapping("/summary")
    clusterSummaryModel getClusterSummary(@RequestHeader("clusterIp") String clusterIp,
                                          @RequestParam(name = "refresh", required = false) Boolean  refresh
                                          ) throws InterruptedException {
        boolean refreshFlag = refresh != null ? refresh : false;

        if(refreshFlag){
            cacheManager.getCache("summary").evict(clusterIp);
        }
        return  this.kafkaMonitorService.getClusterSummary(clusterIp,refreshFlag);
    }

    @GetMapping("/topics")
    List<topicModel> getTopics(@RequestHeader(value = "clusterIp") String clusterIp,
                               @RequestParam(name = "refresh", required = false) Boolean  refresh
                               ){
        boolean refreshFlag = refresh != null ? refresh : false;
        if(refreshFlag){
            cacheManager.getCache("topics").evict(clusterIp);
        }
       return this.kafkaMonitorService.getTopics(clusterIp,false,refreshFlag);

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
        if(refreshFlag){
            cacheManager.getCache("consumers").evict(clusterIp);
        }
        final  var topics = this.kafkaMonitorService.getTopics(clusterIp,false,refreshFlag);
        return this.kafkaMonitorService.getConsumers(topics,clusterIp,refreshFlag);
    }

    @GetMapping("/messages/{name:.+}")
    List<messageModel> getMessages(@RequestHeader(value = "clusterIp") String clusterIp, @PathVariable(value = "name") String name,
                                   @RequestParam(name = "size", required = false) Integer size,
                                   @RequestParam(name = "start", required = false) Long  start,
                                   @RequestParam(name = "end", required = false) Long  end
                                   ){
        final int count = (size != null? size : 200);
        if(start == null && end == null){
            return this.kafkaMonitorService.getLatestMessages(name,clusterIp,count);
        }
        else if(end == null){
            return this.kafkaMonitorService.getMessages(name,clusterIp,count,start);
        }
        else if(start == null){
            return this.kafkaMonitorService.getMessagesUntilTime(name,clusterIp,count,end);
        }
        else{
            return this.kafkaMonitorService.getMessages(name,clusterIp,count,start,end);
        }

    }

    @GetMapping("/lastOffsets/{name:.+}")
    Map<TopicPartition,Long> getLastOffsets(@RequestHeader(value = "clusterIp") String clusterIp, @PathVariable(value = "name") String name
    ){
        return this.kafkaMonitorService.getLastOffsetPerPartition(name,clusterIp);
    }
}
