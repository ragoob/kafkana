import { HttpClient, HttpHeaders } from "@angular/common/http";
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

    getSummary(clusterIp: string): Promise<ClusterSummary>{
        return this.http.get<ClusterSummary>(`${this.baseUrl}/monitoring/summary`, this.header(clusterIp))
        .toPromise();
    }

    getTopics(clusterIp: string): Promise<Topic[]>{
        return this.http.get<Topic[]>(`${this.baseUrl}/monitoring/topics`, this.header(clusterIp))
            .toPromise();
    }

    getTopic(topicName: string ,clusterIp: string): Promise<Topic> {
        return this.http.get<Topic>(`${this.baseUrl}/monitoring/topics/${topicName}`, this.header(clusterIp))
            .toPromise();
    }

    getMessages(topic: string, clusterIp: string,size?: number): Promise<Message[]>{
        const url = size ? `${this.baseUrl}/monitoring/getLatestMessages/${topic}?size=${size}` : `${this.baseUrl}/monitoring/getLatestMessages/${topic}`;
        
        return this.http.get<Message[]>(url, this.header(clusterIp))
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

    getLatestMessages(topic: string, clusterIp: string): Promise<Message[]> {
        return this.http.get<Message[]>(`${this.baseUrl}/monitoring/getLatestMessages/${topic}`,
            this.header(clusterIp)
        )
            .pipe(map(m => {
                m.forEach(msg => {
                    if (msg) {
                        try {
                            msg.fromatedMessage = JSON.parse(msg.message);
                        } catch (error) {
                            msg.fromatedMessage = null;
                        }

                        if (!msg.key) {
                            msg.key = 'None ' + msg.timestamp
                        }
                    }

                });
                return m;
            }))
            .toPromise();
    }

    getConsumers(clusterIp: string): Promise<Consumer[]>{
        return this.http.get<Consumer[]>(`${this.baseUrl}/monitoring/consumers`, this.header(clusterIp))
            .toPromise();
    }

    private header(clusterIp: string): {} {
        const headers = new HttpHeaders();
        headers.append('clusterIp', clusterIp);
        const options = { headers: headers };
        return options;
    }
    
}