import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Inject, Injectable } from "@angular/core";
import { API_BASE_URL } from "../constants";
import { Brokers } from "../models/brokers.model";
import { CreateTopic } from "../models/createTopic.model";
import { KafkaCluster } from "../models/kafka-cluster.model";
import { AdminService } from "./admin.service";

@Injectable()
export class KafkaAdminService  {
    constructor(private http: HttpClient,
        private readonly adminService: AdminService,
        @Inject(API_BASE_URL) private baseUrl?: string,
    
    ) {
    }
    
    createTopic(model: CreateTopic, clusterId: string): Promise<object>{
        return this.http.post<any>(`${this.baseUrl}/kafkaAdmin`,model,this.header(clusterId)).toPromise();
    }

    deleteTopic(name: string, clusterId: string): Promise<object> {
        return this.http.delete<any>(`${this.baseUrl}/kafkaAdmin/${name}`, this.header(clusterId)).toPromise();
    }

    getConfig(nodeId: string, clusterId: string): Promise<Map<string,string>>{
        return this.http.get<Map<string, string>>(`${this.baseUrl}/kafkaAdmin/config/${nodeId}`
            , this.header(clusterId)
        ).toPromise();
    }

    getNodes(clusterId: string): Promise<Brokers[]> {
        return this.http.get<Brokers[]>(`${this.baseUrl}/kafkaAdmin/nodes`
            , this.header(clusterId)
        ).toPromise();
    }


    private header(clusterId: string): {}{
        const cluster = this.adminService.findByid(clusterId);
        const headers = new HttpHeaders({ 'clusterIp': cluster.bootStrapServers });
        const options = { headers: headers };
        return options;
    }

}