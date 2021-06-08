import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ConfirmationService } from 'primeng/api';
import { ReplaySubject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { Topic } from '../../core/models/topic.model';
import { KafkaMonitorService } from '../../core/services/kafka-monitor.service';
import { LoadingService } from '../../core/services/loading.service';

@Component({
  selector: 'app-topic-list',
  templateUrl: './topic-list.component.html',
  styleUrls: ['./topic-list.component.scss']
})
export class TopicListComponent implements OnInit, OnDestroy {
  clusterId: string="";
  first = 0;
  rows = 5;
  loaded: boolean = false;
  public topics: Topic[] = [];
  private destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  constructor(private monitoringService: KafkaMonitorService, private confirmationService: ConfirmationService, 
    private route: ActivatedRoute, private router: Router,
    private loader: LoadingService
    
    ) { }
  ngOnDestroy(): void {
    this.destoryed$.complete();
  }

  ngOnInit(): void {
    this.route.params.
      pipe(takeUntil(this.destoryed$))
      .subscribe(params => {
        this.clusterId = params.id;
        this.loadTopics(params.id);
      });


    this.loader.loaded$
      .pipe(takeUntil(this.destoryed$),
        filter(data => data.context === 'TOPIC_LIST')
      ).subscribe(d => {
        this.loaded = d.loaded;
      })
  }

  public loadTopics(clusterId: string,refresh: boolean = false) {
    this.loader.change('TOPIC_LIST', false);
    this.monitoringService.getTopics(clusterId, refresh)
      .then(data => {
        this.topics = data;
        this.loader.change('TOPIC_LIST', true);
      }).catch(error=> {
        this.loader.change('TOPIC_LIST', true);
        this.topics  = [];
      })
  }

  public deleteTopic(name: string) {
    alert("Under dev");
  }


  public details(name: string){
    this.router.navigate(['/topic-details',this.clusterId,name]);
  }

  public addNewTopic(): void{
    alert("Under dev")
  }

}
