import { HttpClient } from "@angular/common/http";
import { Inject, Injectable } from "@angular/core";
import { ClusterSummary } from "../models/cluster-summary.model";
import { Message } from "../models/message.model";
import { Topic } from "../models/topic.model";
import { map} from 'rxjs/operators'
import { Consumer } from "../models/consumer-model";
import { API_BASE_URL } from "../constants";
@Injectable()
export class KafkaMonitorService   {
  
    constructor(private http: HttpClient, @Inject(API_BASE_URL) private baseUrl?: string) {

    }

    getSummary(clusterId: string): Promise<ClusterSummary>{
        return this.http.get<ClusterSummary>(`${this.baseUrl}/monitoring/${clusterId}/summary`)
        .toPromise();
    }

    getTopics(clusterId: string): Promise<Topic[]>{
        return this.http.get<Topic[]>(`${this.baseUrl}/monitoring/${clusterId}/topics`)
            .toPromise();
    }

    getTopic(topicName: string ,clusterId: string): Promise<Topic> {
        return this.http.get<Topic>(`${this.baseUrl}/monitoring/${clusterId}/topics/${topicName}`)
            .toPromise();
    }

    getMessages(topic: string, clusterId: string): Promise<Message[]>{
        return this.http.get<Message[]>(`${this.baseUrl}/monitoring/${clusterId}/messages/${topic}`)
            .pipe(map(m=> {
               m.forEach(msg=> {
                   if (msg) {
                       try {
                           msg.fromatedMessage = JSON.parse(msg.message);
                       } catch (error) {
                           msg.fromatedMessage = null;
                       }

                       if(!msg.key){
                           msg.key = 'None ' + msg.timestamp
                       }
                   }
                 
               });
               return m;
            }))
            .toPromise();
    }

    getConsumers(clusterId: string): Promise<Consumer[]>{
        return this.http.get<Consumer[]>(`${this.baseUrl}/monitoring/${clusterId}/consumers`)
            .toPromise();
    }
    
}