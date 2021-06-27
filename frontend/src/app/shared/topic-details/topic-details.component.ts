import { AfterContentChecked, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReplaySubject, fromEvent  } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Topic } from '../../core/models/topic.model';
import { KafkaMonitorService } from '../../core/services/kafka-monitor.service';
import { debounceTime } from 'rxjs/operators';

@Component({
  selector: 'app-topic-details',
  templateUrl: './topic-details.component.html',
  styleUrls: ['./topic-details.component.scss']
})
export class TopicDetailsComponent implements OnInit, OnDestroy {
  @ViewChild('countInput', { static: true }) countInput?: ElementRef ;

  clusterId: string = "";
  private destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  public topic?: Topic
  active: number = 1;
  loaded: boolean = false;
  constructor(private monitoringService: KafkaMonitorService, private route: ActivatedRoute) { }
 

  ngOnDestroy(): void {
    this.destoryed$.complete();
  }

  ngOnInit(): void {
    this.route.params.
      pipe(takeUntil(this.destoryed$))
      .subscribe(params => {
        this.clusterId = params.id;
        this.loadTopic(params.topicId);
       
      })
  }

  public loadTopic(topicName?: string) {
    this.loaded = false;
    this.monitoringService.getTopic(topicName?? '',this.clusterId)
      .then(data => {
        this.topic = data;
        
      }).catch(error=> {
        this.topic = new Topic();
      }).finally(() => this.loaded = true);
  }

  

}
