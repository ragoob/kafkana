export class PartitionReplica{
    constructor(){
        this.id= 0;
        this.inSync= false;
        this.leader = false;
        this.offline = false;
    }
    id: number;
    inSync: boolean;
    leader: boolean;
    offline: boolean;
}