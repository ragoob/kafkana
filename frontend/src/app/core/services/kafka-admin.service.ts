import { HttpClient } from "@angular/common/http";
import { Inject, Injectable } from "@angular/core";
import { API_BASE_URL } from "../constants";
import { Brokers } from "../models/brokers.model";
import { CreateTopic } from "../models/createTopic.model";

@Injectable()
export class KafkaAdminService  {
    constructor(private http: HttpClient, @Inject(API_BASE_URL) private baseUrl?: string) {
    }
    
    createTopic(model: CreateTopic, clusterId: string): Promise<object>{
        return this.http.post<any>(`${this.baseUrl}/kafkaAdmin/${clusterId}`,model).toPromise();
    }

    deleteTopic(name: string, clusterId: string): Promise<object> {
        return this.http.delete<any>(`${this.baseUrl}/kafkaAdmin/${clusterId}/${name}`).toPromise();
    }

    getConfig(nodeId: string, clusterId: string): Promise<Map<string,string>>{
        return this.http.get<Map<string, string>>(`${this.baseUrl}/kafkaAdmin/${clusterId}/config/${nodeId}`).toPromise();
    }

    getNodes(clusterId: string): Promise<Brokers[]> {
        return this.http.get<Brokers[]>(`${this.baseUrl}/kafkaAdmin/${clusterId}/nodes`).toPromise();
    }


}