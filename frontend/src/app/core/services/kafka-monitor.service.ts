import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Inject, Injectable } from "@angular/core";
import { ClusterSummary } from "../models/cluster-summary.model";
import { Message } from "../models/message.model";
import { Topic } from "../models/topic.model";
import { map} from 'rxjs/operators'
import { Consumer } from "../models/consumer-model";
import { API_BASE_URL } from "../constants";
import { AdminService } from "./admin.service";
import { KafkaCluster } from "../models/kafka-cluster.model";
@Injectable()
export class KafkaMonitorService   {
  
    constructor(private http: HttpClient,
        private readonly adminService: AdminService,
        @Inject(API_BASE_URL) private baseUrl?: string
        
    ) {

    }

    getSummary(clusterId: string, refresh: boolean = false): Promise<ClusterSummary>{
        return this.http.get<ClusterSummary>(`${this.baseUrl}/monitoring/summary?refresh=${refresh}`, this.header(clusterId))
        .toPromise();
    }

    getTopics(clusterId: string, refresh: boolean = false): Promise<Topic[]>{
        return this.http.get<Topic[]>(`${this.baseUrl}/monitoring/topics?refresh=${refresh}`, this.header(clusterId))
            .toPromise();
    }

    getTopic(topicName: string ,clusterId: string): Promise<Topic> {
        return this.http.get<Topic>(`${this.baseUrl}/monitoring/topics/${topicName}?showDefaultConfig=true`, this.header(clusterId))
            .toPromise();
    }

    getMessages(topic: string, clusterId: string,size?: number,start?: number,end?: number,partition?: number,sortDir: 'asc' | 'desc' = 'asc'): Promise<Message[]>{
        
        if(!size){
            size = 0;
        }
        let url = `${this.baseUrl}/monitoring/messages/${topic}?size=${size}`;
        if(start){
            url += `&start=${start}`
        }
        if(end || end == 0){
            url += `&end=${end}`
        }

        if(partition){
            url += `&partition=${partition}`
        }

        url += `&sortDirection=${sortDir}`
        return this.http.get<Message[]>(url, this.header(clusterId))
            .pipe(map(m=> {
               m.forEach(msg=> {
                   if (msg) {
                       try {
                           msg.fromatedMessage = JSON.parse(msg.message);
                       } catch (error) {
                           msg.fromatedMessage = null;
                       }

                       if(!msg.key){
                           msg.key = 'None'
                       }
                   }
                 
               });
               return m;
            }))
            .toPromise();
    }

    getConsumers(clusterId: string, refresh: boolean = false): Promise<Consumer[]>{
        return this.http.get<Consumer[]>(`${this.baseUrl}/monitoring/consumers?refresh=${refresh}`, this.header(clusterId))
            .toPromise();
    }

    private header(clusterId: string): {} {
        const cluster = this.adminService.findByid(clusterId);
        const headers = new HttpHeaders({ 'clusterIp': cluster.bootStrapServers });
        const options = { headers: headers };
        return options;
    }
    
}