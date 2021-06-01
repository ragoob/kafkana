export class Brokers{
    constructor(){
        this.id = -1;
        this.port = 0;
        this.host = "";
        this.rack = "";
        this.config = new Map<string,string>();
        this.isController = false;
    }
    id: number;
    host: string;
    port : number;
    rack: string;
    config: Map<string,string>;
    isController: boolean;
}