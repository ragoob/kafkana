import { Component, Input, OnInit } from '@angular/core';
import { ReplaySubject } from 'rxjs';
import { Message } from '../../core/models/message.model';
import { Topic } from '../../core/models/topic.model';
import { KafkaMonitorService } from '../../core/services/kafka-monitor.service';

@Component({
  selector: 'app-topic-messages',
  templateUrl: './topic-messages.component.html',
  styleUrls: ['./topic-messages.component.scss']
})
export class TopicMessagesComponent implements OnInit {

  @Input('clusterId') clusterId: string = "";
  private destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  @Input('topic') topic?: Topic
  messages: Message[] = [];
  constructor(private monitoringService: KafkaMonitorService) { }



  ngOnDestroy(): void {
    this.destoryed$.complete();
  }

  ngOnInit(): void {
    this.loadmessages(this.topic?.name ?? "", this.clusterId);
  }

  private loadmessages(topicName: string, clusterId: string) {
    this.monitoringService.getMessages(topicName, clusterId)
      .then(data => {
        this.messages = data;
      })
  }

}
