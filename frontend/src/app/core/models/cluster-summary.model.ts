export class ClusterSummary{
    constructor(){
        this.topicCount = 0;
        this.partitionCount = 0;
        this.underReplicatedCount = 0;
        this.preferredReplicaPercent = 0;
        this.brokerCount = 0;
        this.expectedBrokerIds = [];
        this.brokerPreferredLeaderPartitionCount = new Map<string,number>();
    }
    topicCount: number;
    partitionCount: number;
    underReplicatedCount: number;
    preferredReplicaPercent: number;
    brokerCount: number;
    expectedBrokerIds: number[];
    brokerPreferredLeaderPartitionCount: Map<string,number>;
    timeStamp?: Date
}