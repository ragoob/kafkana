package com.kafkana.backend.controllers;

import com.kafkana.backend.abstraction.kafkaAdminService;
import com.kafkana.backend.abstraction.kafkaMonitorService;
import com.kafkana.backend.models.clusterSummaryModel;
import com.kafkana.backend.models.consumerModel;
import com.kafkana.backend.models.messageModel;
import com.kafkana.backend.models.topicModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.PathParam;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController()
@CrossOrigin()
@RequestMapping("/api/monitoring/{clusterId}")
public class kafkaMonitoringController {
    @Autowired
    private kafkaMonitorService kafkaMonitorService ;

    @Autowired
    private kafkaAdminService kafkaAdminService;


    @GetMapping("/summary")
    clusterSummaryModel getClusterSummary(@PathVariable(value = "clusterId") String clusterId) throws InterruptedException {
        final  var topics = this.kafkaMonitorService.getTopics(clusterId);
        return  this.kafkaMonitorService.getClusterSummary(topics);
    }

    @GetMapping("/topics")
    List<topicModel> getTopics(@PathVariable(value = "clusterId") String clusterId){
       return this.kafkaMonitorService.getTopics(clusterId);

    }

    @GetMapping("/topics/{name:.+}")
    Optional<topicModel> getTopic(@PathVariable(value = "clusterId") String clusterId, @PathVariable(value = "name") String name){
        return this.kafkaMonitorService.getTopic(name,clusterId);

    }

    @GetMapping("/consumers")
    List<consumerModel> getConsumers(@PathVariable(value = "clusterId") String clusterId){
        final  var topics = this.kafkaMonitorService.getTopics(clusterId);
        return this.kafkaMonitorService.getConsumers(topics,clusterId);
    }

    @GetMapping("/messages/{name:.+}")
    List<messageModel> getMessages(@PathVariable(value = "clusterId") String clusterId, @PathVariable(value = "name") String name,
                                   @RequestParam(name = "size", required = false) Integer size,
                                   @RequestParam(name = "start", required = false) Long  start,
                                   @RequestParam(name = "end", required = false) Long  end
                                   ){
        final int count = (size != null? size : 200);
        if(start == null || end == null){
            return this.kafkaMonitorService.getMessages(name,clusterId,count);
        }
        else{
            return this.kafkaMonitorService.getMessages(name,clusterId,count,start,end);
        }

    }



}
