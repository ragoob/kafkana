package com.kafkana.backend.controllers;
import com.kafkana.backend.configurations.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("")
public class homeController {
    @Autowired
    private  AppConfig config;
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public  String get() {
      return  "Welcome Kafkana";
    }
    @GetMapping("/config")
    @ResponseStatus(HttpStatus.OK)
    public AppConfig config(){
        return  this.config;
    }
}
