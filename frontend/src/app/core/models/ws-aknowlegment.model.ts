import * as Stomp from 'stompjs';

export class WsAcknowledgment{
    constructor(topic: string, event?: Stomp.Message) {
        this.topic = topic;
        this.event = event;
    }
    topic: string;
    event?: Stomp.Message;
}