package com.kafkana.backend.controllers;
import com.kafkana.backend.abstraction.kafkaAdminService;
import com.kafkana.backend.configurations.AppConfig;
import com.kafkana.backend.models.brokers;
import com.kafkana.backend.models.createTopicModel;
import jdk.jshell.spi.ExecutionControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;

@RestController()
@CrossOrigin
@RequestMapping("/api/kafkaAdmin")
public class kafkaAdminController {
    @Value("${kafka.allowtopicscreation}")
    private  boolean allowtopicscreation;

    @Value("${kafka.allowtopicsdeletion}")
    private  boolean allowtopicsdeletion;

    @Autowired
    private kafkaAdminService kafkaAdminService;
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    void  create(@RequestHeader(value = "clusterIp") String clusterIp, @RequestBody() createTopicModel model) throws ExecutionControl.NotImplementedException {
      if(!allowtopicscreation)
          throw  new ExecutionControl.NotImplementedException("Creating topics is disabled on this instance");
       this.kafkaAdminService.create(model,clusterIp);
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    void  delete(@RequestHeader(value = "clusterIp") String clusterIp, @PathVariable(value = "name") String name) throws ExecutionControl.NotImplementedException {
        if(!allowtopicsdeletion)
            throw  new ExecutionControl.NotImplementedException("deleting topics is disabled on this instance");
        this.kafkaAdminService.delete(new createTopicModel(name),clusterIp);
    }

    @GetMapping("/config/{nodeId}")
    HashMap<String,String> getConfig(@RequestHeader(value = "clusterIp") String clusterIp,@PathVariable(value = "nodeId") String nodeId,
     @RequestParam(name = "refresh", required = false) Boolean  refresh
    ){

        return  this.kafkaAdminService.getConfig(clusterIp,nodeId);
    }

    @GetMapping("/nodes")
    ArrayList<brokers> getNodes(@RequestHeader(value = "clusterIp") String clusterIp,
                                @RequestParam(name = "refresh", required = false) Boolean  refresh
                                ){
        return  this.kafkaAdminService.getBrokers(clusterIp);
    }

    @GetMapping("/health-check")
    boolean healthCheck(@RequestHeader(value = "clusterIp") String clusterIp){
        return this.kafkaAdminService.IsHealth(clusterIp);
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.CREATED)
    void  deleteConsumer(@RequestHeader(value = "clusterIp") String clusterIp,@PathVariable(value = "id") String id ) {
        this.kafkaAdminService.deleteConsumer(clusterIp,id);
    }

}
