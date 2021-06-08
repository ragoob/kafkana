package com.kafkana.backend.controllers;
import com.kafkana.backend.abstraction.kafkaAdminService;
import com.kafkana.backend.models.brokers;
import com.kafkana.backend.models.createTopicModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;

@RestController()
@CrossOrigin
@RequestMapping("/api/kafkaAdmin")
public class kafkaAdminController {
    @Autowired
    private kafkaAdminService kafkaAdminService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    void  create(@RequestHeader(value = "clusterIp") String clusterIp, @RequestBody() createTopicModel model) {
       this.kafkaAdminService.create(model,clusterIp);
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    void  delete(@RequestHeader(value = "clusterIp") String clusterIp, @PathVariable(value = "name") String name) {
        this.kafkaAdminService.delete(new createTopicModel(name),clusterIp);
    }

    @GetMapping("/config/{nodeId}")
    @Cacheable(value="nodeConfig",
            key="{#clusterIp, #nodeId}"
            , condition="#refresh == false")
    HashMap<String,String> getConfig(@RequestHeader(value = "clusterIp") String clusterIp,@PathVariable(value = "nodeId") String nodeId,
     @RequestParam(name = "refresh", required = false) Boolean  refresh
    ){
        return  this.kafkaAdminService.getConfig(clusterIp,nodeId);
    }

    @GetMapping("/nodes")
    @Cacheable(value="nodes",
            key="{#clusterIp}"
            , condition="#refresh == false")
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
