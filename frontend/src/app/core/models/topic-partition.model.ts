import { Brokers } from "./brokers.model";
import { PartitionReplica } from "./partition-replica.model";

export class TopicPartition{
    constructor(){
        this.id = 0;
        this.replicas = [];
        this.leaderId = 0;
        this.size = -1;
        this.firstOffset = -1;
        this.inSyncReplicas = [];
        this.offlineReplicas = [];
        
    }
    id: number;
    replicas: PartitionReplica[];
    leaderId: number;
    size: number;
    firstOffset: number;
    inSyncReplicas: PartitionReplica[];
    offlineReplicas: PartitionReplica[];
    leader?: Brokers;
    preferredLeader?: Brokers;
}