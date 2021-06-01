import { ConsumerTopic } from "./consumer-topic.model";

export class Consumer{
    constructor() {
        this.groupId = "";
        this.topics = [];
    }
    groupId: string;
    topics: ConsumerTopic[];
}