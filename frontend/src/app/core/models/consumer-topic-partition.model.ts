export class ConsumerTopicPartition{
    constructor(){
        this.partitionId = -1;
        this.offset = -1;
        this.size = -1;
        this.firstOffset = -1;
        this.lag = -1;
    }
    partitionId: number;
    offset: number;
    size: number;
    firstOffset: number;
    lag: number;
}