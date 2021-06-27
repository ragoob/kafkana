import { Injectable } from "@angular/core";
import { KafkaConfig } from "../models/kafka-config.model";
import { AdminService } from "./admin.service";

@Injectable({providedIn: 'root'})
export  class ConfigService{
    public config?: KafkaConfig;
  constructor(private adminService: AdminService){
    this.adminService.getConfiguration()
        .then(data => this.config = data);
  }
}