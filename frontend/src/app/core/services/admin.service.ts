import { HttpClient } from "@angular/common/http";
import { Inject, Injectable } from "@angular/core";
import { API_BASE_URL } from "../constants";
import { KafkaCluster } from "../models/kafka-cluster.model";

@Injectable()
export class AdminService{
    constructor(private http: HttpClient, @Inject(API_BASE_URL) private baseUrl?: string){
    }

    getAll(): Promise<KafkaCluster[]>{
      return  this.http.get <KafkaCluster[]>(`${this.baseUrl}/admin`)
        .toPromise();
    }

    create(model: KafkaCluster): Promise<Object>{
        return this.http.post(`${this.baseUrl}/admin`,model)
        .toPromise();
    }

    delete(id: number): Promise<Object> {
        return this.http.delete(`${this.baseUrl}/admin/${id}`)
            .toPromise();
    }


}