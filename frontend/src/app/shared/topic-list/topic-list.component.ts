import { ThrowStmt } from '@angular/compiler';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { ConfirmationService } from 'primeng/api';
import { BehaviorSubject, ReplaySubject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { Topic } from '../../core/models/topic.model';
import { KafkaAdminService } from '../../core/services/kafka-admin.service';
import { KafkaMonitorService } from '../../core/services/kafka-monitor.service';
import { LayoutUtilsService, MessageType } from '../../core/services/layout-utils.service';
import { LoadingService } from '../../core/services/loading.service';
import { TopicCreateComponent } from '../topic-create/topic-create.component';

@Component({
  selector: 'app-topic-list',
  templateUrl: './topic-list.component.html',
  styleUrls: ['./topic-list.component.scss']
})
export class TopicListComponent implements OnInit, OnDestroy {
  clusterId: string="";
  first = 0;
  rows = 25;
  loaded: boolean = false;
  public topics: Topic[] = [];
  private destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  public waitUntilDelete$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  constructor(private monitoringService: KafkaMonitorService, 
    private route: ActivatedRoute, private router: Router,
    private loader: LoadingService,
    public dialog: MatDialog,
    public layoutService: LayoutUtilsService,
    private kafkaAdminService: KafkaAdminService

    
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
    let messge: string = '';
    const dialogRef = this.layoutService.deleteElement(`Delete topic ${name}`, 'Are you sure to delete topic ?', 'Please wait ...', this.waitUntilDelete$);
    dialogRef.componentInstance.onDelete
    .pipe(takeUntil(this.destoryed$))
    .subscribe(() => { 
       this.kafkaAdminService.deleteTopic(name,this.clusterId)
       .then(res=> {
         messge = 'Topic deleted successfully';
       }).catch(error=> {
         messge = `Error on deleting the topic ${error.error}`;
       }).finally(()=>{
         this.layoutService.showActionNotification(messge, MessageType.Delete);
         this.waitUntilDelete$.next(true);
       })
    });
   
  }

 
  public details(name: string){
    this.router.navigate(['/topic-details',this.clusterId,name]);
  }



  public addNewTopic(): void{
    const dialogRef = this.dialog.open(TopicCreateComponent, {
      width: '50%',
      panelClass: 'kt-mat-dialog-container__wrapper',
      data: { clusterId: this.clusterId}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadTopics(this.clusterId,true);
      }
    });
  }

}
