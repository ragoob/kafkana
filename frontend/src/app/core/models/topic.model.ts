import { TopicPartition } from "./topic-partition.model";

export class Topic{
    constructor(){
        this.name = "";
        this.partitions = [];
        this.config = new Map<string,string>();
        this.preferredReplicaPercent = 0;
        this.availableSize = 0;
        this.totalSize =0;


    }
    name: string;
    partitions: TopicPartition[];
    config: Map<string,string>;
    preferredReplicaPercent: number;
    availableSize: number;
    totalSize: number;


}