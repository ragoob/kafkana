export class ConsumerMember{
    constructor(){
        this.id = ""
        this.clientId = ""
        this.host = ""
        this.topic = ""
        this.partition = 0
        this.lastCommittedOffset  = 0
        this.endOffsets = 0
        this.lag = 0
    }
    id: string;
    host: string;
    clientId: string;
    topic: string;
    partition: number;
    lastCommittedOffset: number;
    endOffsets: number;
    lag: number;

}