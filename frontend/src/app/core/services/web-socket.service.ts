import { Inject, Injectable } from "@angular/core";
import { SOCKET_URL } from "../constants";
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';
import { BehaviorSubject } from "rxjs";
import { WsAcknowledgment } from "../models/ws-aknowlegment.model";
import { WsOnError } from "../models/ws-on-error.model";
@Injectable({providedIn: 'root'})
export class WebSocketService{
    public aka$: BehaviorSubject<WsAcknowledgment> = new BehaviorSubject(new WsAcknowledgment('',undefined));
    public onError$: BehaviorSubject<WsOnError> = new BehaviorSubject(new WsOnError());
    private stompClient: Stomp.Client;

    constructor(@Inject(SOCKET_URL) private SocketURL: string){
        const ws = new SockJS(this.SocketURL);
        this.stompClient = Stomp.over(ws);
    }

    connect(topic: string): Promise<void> {
        return new Promise((resolve, _) => {
            const _this = this;
            _this.stompClient?.connect({}, (frame: any) => {
                _this.stompClient
                .subscribe(`/topic/${topic}`, (event: Stomp.Message)=> {
                    _this.aka$.next(new WsAcknowledgment(topic,event));
                });
                resolve();
            }, (error: any)=> {
                this.onError$.next(error);
            });
        })
    };

    disconnect(): Promise<any> {
        return new Promise((resolve,reject)=> {
            if (this.stompClient !== null) {
                this.stompClient.disconnect(() => {
                    resolve("Disconnected from ws");
                });
            }

        })
       
    }

    send(topic: string, event: any) {
        this.stompClient.send(`/app/${topic}`, {}, JSON.stringify(event));
    }

}