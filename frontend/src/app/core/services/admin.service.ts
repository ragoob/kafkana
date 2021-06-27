import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Inject, Injectable } from "@angular/core";
import { API_BASE_URL } from "../constants";
import { KafkaCluster } from "../models/kafka-cluster.model";
import { KafkaConfig } from "../models/kafka-config.model";

@Injectable()
export class AdminService{
    constructor(private http: HttpClient, @Inject(API_BASE_URL) private baseUrl?: string){
    }

    getAll(): Promise<KafkaCluster[]>{
      return new Promise((resolve,reject)=>{
          const list = localStorage.getItem("CLUSTER_LIST")?? "[]";
          resolve(JSON.parse(list));
      });
    }

    findByid(id: string): KafkaCluster  {
        const list = JSON.parse(localStorage.getItem("CLUSTER_LIST") ?? "[]") as KafkaCluster[];
        return list.find(l => l.id === id) ?? new KafkaCluster;
    }

    create(model: KafkaCluster): Promise<void>{
        return new Promise((resolve,reject)=> {
            const list = JSON.parse(localStorage.getItem("CLUSTER_LIST") ?? "[]") as KafkaCluster[];
            list.push(model);
            resolve(localStorage.setItem("CLUSTER_LIST",JSON.stringify(list)));
        })
    }

    update(id:string ,model: KafkaCluster): Promise<void> {
        return new Promise((resolve, reject) => {
            let list = JSON.parse(localStorage.getItem("CLUSTER_LIST") ?? "[]") as KafkaCluster[];
            const index = list.findIndex(l => l.id == id);
            list[index].id = model.id;
            list[index].bootStrapServers = model.bootStrapServers;
            list[index].status = model.status;
            resolve(localStorage.setItem("CLUSTER_LIST", JSON.stringify(list)));
        })
    }

    delete(id: string): Promise<void> {
       return new Promise((resolve,reject)=>{
           const list = JSON.parse(localStorage.getItem("CLUSTER_LIST") ?? "[]") as KafkaCluster[];
           const index = list.findIndex(l=> l.id === id);
           list.splice(index,1);
           resolve(localStorage.setItem("CLUSTER_LIST", JSON.stringify(list)));
       })
    }

    getCurrent(): string{
        const cluster = localStorage.getItem("CURRENT_CLUSTER") ?? "";
        return cluster;
    }

    setCurrent(id?: string){
        localStorage.setItem("CURRENT_CLUSTER",id?? "");
    }

    healthCheck(clusterId: string): Promise<boolean> {
        return this.http.get<boolean>(`${this.baseUrl}/kafkaAdmin/health-check`
            , this.header(clusterId)
        ).toPromise();
    }

    getConfiguration(): Promise<KafkaConfig> {
        return this.http.get<KafkaConfig>(`${this.baseUrl}/admin`

        ).toPromise();
    }

    private header(clusterId: string): {} {
        const cluster = this.findByid(clusterId);
        const headers = new HttpHeaders({ 'clusterIp': cluster.bootStrapServers });
        const options = { headers: headers };
        return options;
    }


}