import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReplaySubject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Topic } from '../../core/models/topic.model';
import { KafkaMonitorService } from '../../core/services/kafka-monitor.service';

@Component({
  selector: 'app-topic-details',
  templateUrl: './topic-details.component.html',
  styleUrls: ['./topic-details.component.scss']
})
export class TopicDetailsComponent implements OnInit, OnDestroy{
  clusterId: string = "";
  private destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  public topic?: Topic
  active: number = 1;
  constructor(private monitoringService: KafkaMonitorService, private route: ActivatedRoute) { }

  

  ngOnDestroy(): void {
    this.destoryed$.complete();
  }

  ngOnInit(): void {
    this.route.params.
      pipe(takeUntil(this.destoryed$))
      .subscribe(params => {
        this.clusterId = params.id;
        this.loadTopic(params.id, params.topicId);
       
      })
  }

  private loadTopic(topicName: string, clusterId: string) {
    this.monitoringService.getTopic(topicName,clusterId)
      .then(data => {
        this.topic = data;
      })
  }

}
