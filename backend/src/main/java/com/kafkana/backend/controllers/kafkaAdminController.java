package com.kafkana.backend.controllers;
import com.kafkana.backend.abstraction.kafkaAdminService;
import com.kafkana.backend.models.brokers;
import com.kafkana.backend.models.createTopicModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;

@RestController()
@CrossOrigin()
@RequestMapping("/api/kafkaAdmin/{clusterId}")
public class kafkaAdminController {
    @Autowired
    private kafkaAdminService kafkaAdminService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    void  create(@PathVariable(value = "clusterId") String clusterId, @RequestBody() createTopicModel model) {
       this.kafkaAdminService.create(model,clusterId);
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.OK)
    void  delete(@PathVariable(value = "clusterId") String clusterId, @PathVariable(value = "name") String name) {
        this.kafkaAdminService.delete(new createTopicModel(name),clusterId);
    }

    @GetMapping("/config/{nodeId}")
    HashMap<String,String> getConfig(@PathVariable(value = "clusterId") String clusterId,@PathVariable(value = "nodeId") String nodeId){
        return  this.kafkaAdminService.getConfig(clusterId,nodeId);
    }

    @GetMapping("/nodes")
    ArrayList<brokers> getNodes(@PathVariable(value = "clusterId") String clusterId){
        return  this.kafkaAdminService.getBrokers(clusterId);
    }


}
