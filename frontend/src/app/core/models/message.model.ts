export class Message{
    constructor(){
     this.partition = -1;
     this.offset = -1;
    }
    partition: number;
    offset: number;
    message: any;
    key? : string;
    headers?: Map<string,string>;
    timestamp?: Date;
    headersFormatted? : string;
    fromatedMessage ? : any;

}