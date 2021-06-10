package com.kafkana.backend.controllers;

import com.kafkana.backend.models.kafkaCluster;
import com.kafkana.backend.repositories.kafkaClusterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController()
@CrossOrigin
@RequestMapping("/api/admin")
public class administrationController {
    @Autowired
    private kafkaClusterRepository kafkaClusterRepository;
    @GetMapping()
    ArrayList<kafkaCluster> getAll() {
        return  this.kafkaClusterRepository.getAll();
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity create(@RequestBody() kafkaCluster model) {
        if(!this.kafkaClusterRepository.getById(model.getId()).isEmpty()){
            return new ResponseEntity<>("Cluster already exists", HttpStatus.CONFLICT);
        }
        this.kafkaClusterRepository.add(model);
        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    void  delete(@PathVariable(value = "id") String id) {
        this.kafkaClusterRepository.remove(id);
    }



}
