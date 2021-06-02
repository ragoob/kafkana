import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ConfirmationService } from 'primeng/api';
import { ReplaySubject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Topic } from '../../core/models/topic.model';
import { KafkaAdminService } from '../../core/services/kafka-admin.service';
import { KafkaMonitorService } from '../../core/services/kafka-monitor.service';

@Component({
  selector: 'app-topic-list',
  templateUrl: './topic-list.component.html',
  styleUrls: ['./topic-list.component.scss']
})
export class TopicListComponent implements OnInit, OnDestroy {
  clusterId: string="";
  first = 0;
  rows = 5;
  public topics: Topic[] = [];
  private destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  constructor(private monitoringService: KafkaMonitorService, private confirmationService: ConfirmationService, 
    private kafkaAdminService: KafkaAdminService, 
    private route: ActivatedRoute, private router: Router) { }
  ngOnDestroy(): void {
    this.destoryed$.complete();
  }

  ngOnInit(): void {
    this.route.params.
      pipe(takeUntil(this.destoryed$))
      .subscribe(params => {
        this.clusterId = params.id;
        this.loadTopics(params.id);
      })
  }

  private loadTopics(clusterId: string) {
    this.monitoringService.getTopics(clusterId)
      .then(data => {
        this.topics = data;
      })
  }

  public deleteTopic(name: string) {
    const index = this.topics.findIndex(c=> c.name === name);
      this.confirmationService.confirm({
        message: 'Are you sure that you want to perform this action?',
        accept: () => {
          this.kafkaAdminService.deleteTopic(name, this.clusterId)
            .then(d => {
              this.topics.splice(index,1);
              alert("Topic Deleted");

            }).catch(error=> {
              alert(error)
            })
        }
      });
  }


  public details(name: string){
    this.router.navigate(['/topic-details',this.clusterId,name]);
  }

  public addNewTopic(): void{
    alert("Under dev")
  }

}
