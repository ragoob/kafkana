export class KafkaCluster{
    constructor(){
        this.id = "";
        this.bootStrapServers = "";
        this.status = ClusterStatus.UNKOWN

    }
    id: string;
    bootStrapServers: string;
    status: ClusterStatus

}

export enum ClusterStatus{
    UNKOWN = 'UNKOWN',
    HEALTHY = 'HEALTHY',
    UNHEALTHY = 'UNHEALTHY'
}