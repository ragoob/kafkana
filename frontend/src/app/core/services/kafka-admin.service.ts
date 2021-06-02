import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Inject, Injectable } from "@angular/core";
import { API_BASE_URL } from "../constants";
import { Brokers } from "../models/brokers.model";
import { CreateTopic } from "../models/createTopic.model";

@Injectable()
export class KafkaAdminService  {
    constructor(private http: HttpClient, @Inject(API_BASE_URL) private baseUrl?: string) {
    }
    
    createTopic(model: CreateTopic, clusterIp: string): Promise<object>{
        return this.http.post<any>(`${this.baseUrl}/kafkaAdmin/${clusterIp}`,model,this.header(clusterIp)).toPromise();
    }

    deleteTopic(name: string, clusterIp: string): Promise<object> {
        return this.http.delete<any>(`${this.baseUrl}/kafkaAdmin/${clusterIp}/${name}`, this.header(clusterIp)).toPromise();
    }

    getConfig(nodeId: string, clusterIp: string): Promise<Map<string,string>>{
        return this.http.get<Map<string, string>>(`${this.baseUrl}/kafkaAdmin/${clusterIp}/config/${nodeId}`
            , this.header(clusterIp)
        ).toPromise();
    }

    getNodes(clusterIp: string): Promise<Brokers[]> {
        return this.http.get<Brokers[]>(`${this.baseUrl}/kafkaAdmin/${clusterIp}/nodes`
            , this.header(clusterIp)
        ).toPromise();
    }


    private header(clusterIp: string): {}{
        const headers = new HttpHeaders();
        headers.append('clusterIp', clusterIp);
        const options = { headers: headers };
        return options;
    }

}