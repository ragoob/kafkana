export class Message{
    constructor(){
     this.partition = -1;
     this.offset = -1;
    }
    timestamp?: Date;
    partition: number;
    offset: number;
    key?: string;
    message: any;
    headers?: Map<string,string>;
    headersFormatted? : string;
    fromatedMessage ? : any;

    default(){
        return {
            partition: -1,
            offset: -1,
            message: null,
            key: null,
            headers: null,
            timestamp: null,
            headersFormatted: null,
            fromatedMessage: null

        }
    }

}