import { ConsumerTopicPartition } from "./consumer-topic-partition.model";

export class ConsumerTopic{
    constructor(){
        this.topic = "";
        this.partitions = [];
    }
    topic: string;
    partitions: ConsumerTopicPartition[]
    
}