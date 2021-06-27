package com.kafkana.backend.controllers;

import com.kafkana.backend.configurations.AppConfig;
import com.kafkana.backend.configurations.kafkaConfig;
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
    private AppConfig appConfig;

    @GetMapping()
    kafkaConfig getAppConfig() {
        return  this.appConfig.getKafka();
    }



}
