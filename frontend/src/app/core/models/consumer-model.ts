import { Brokers } from "./brokers.model";
import { ConsumerMember } from "./consumer-member";
import { ConsumerTopic } from "./consumer-topic.model";

export class Consumer{
    constructor() {
        this.groupId = "";
        this.members = [];
        
    }
    groupId: string;
    members: ConsumerMember[]
    broker?: Brokers;
}