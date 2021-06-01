export class CreateTopic{
    constructor(){
        this.partitions = -1;
        this.topicName = "";
        this.replication = 0;
        this.configurations = new Map<string,string>();
    }
    partitions: number;
    topicName: string;
    replication: number;
    configurations: Map<string,string>;

}