import { HttpClient } from "@angular/common/http";
import { Inject, Injectable } from "@angular/core";
import { API_BASE_URL } from "../constants";
import { KafkaCluster } from "../models/kafka-cluster.model";

@Injectable()
export class AdminService{
    constructor(){
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


}