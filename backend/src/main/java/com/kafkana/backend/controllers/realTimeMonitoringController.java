package com.kafkana.backend.controllers;
import com.kafkana.backend.models.WebSocketParamsModel;
import com.kafkana.backend.models.clusterSummaryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;


@Controller
public class realTimeMonitoringController {
    @Autowired
    private com.kafkana.backend.abstraction.kafkaMonitorService kafkaMonitorService ;
    @Autowired(required = false)
    private com.kafkana.backend.abstraction.summaryRepository summaryRepository;
    @Value("${cache.allowed}")
    private  boolean allowCache;

    @MessageMapping("/summary")
    @SendTo("/topic/summary")
    public clusterSummaryModel summary(WebSocketParamsModel params) throws Exception {
        clusterSummaryModel summaryModel;
        if(allowCache){
            var cached = this.summaryRepository.find(params.getClusterIp());
            summaryModel = cached == null || params.isRefresh() ?
                    this.kafkaMonitorService.getClusterSummary(params.getClusterIp()) : cached;
            this.summaryRepository.save(summaryModel,params.getClusterIp());
        }else{
            summaryModel  = this.kafkaMonitorService.getClusterSummary(params.getClusterIp());
        }

        return summaryModel;
    }
}
