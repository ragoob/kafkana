import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReplaySubject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { ClusterSummary } from '../../core/models/cluster-summary.model';
import { KafkaCluster } from '../../core/models/kafka-cluster.model';
import { SocketTopics } from '../../core/models/socket-topics.enum';
import { AdminService } from '../../core/services/admin.service';
import { LoadingService } from '../../core/services/loading.service';
import { WebSocketService } from '../../core/services/web-socket.service';

@Component({
  selector: 'app-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.scss']
})
export class SummaryComponent implements OnInit ,OnDestroy{
  cluster?: KafkaCluster;
  private destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  public loaded: boolean = false;
  public summary?: ClusterSummary | null
  constructor(
    private route: ActivatedRoute,private router: Router,
    private loader: LoadingService,
    private webSocketService: WebSocketService,
    private adminService: AdminService
    ) { }

  ngOnDestroy(): void {
    this.destoryed$.complete();
     this.webSocketService.disconnect()
     .then((msg)=> console.log(msg));
  }

  ngOnInit(): void {
    this.webSocketService.aka$
      .pipe(filter(ws => ws.topic == SocketTopics.SUMMARY),
     takeUntil(this.destoryed$)
    ).subscribe(msg=> {
      this.loader.change('SUMMARY_LIST', true);
      if (msg.event){
       this.summary = JSON.parse(msg.event?.body);
      }
    });

    this.webSocketService.onError$
      .pipe(filter(ws => ws.topic == SocketTopics.SUMMARY),
        takeUntil(this.destoryed$)
      ).subscribe(error=> {
        this.loader.change('SUMMARY_LIST', true);
      });
  
    this.route.params.
    pipe(takeUntil(this.destoryed$))
    .subscribe(params=> {
      this.cluster = this.adminService.findByid(params.id);
      this.loader.change('SUMMARY_LIST', false);
      this.webSocketService
        .connect(SocketTopics.SUMMARY)
        .then(() => {
          this.webSocketService.send(SocketTopics.SUMMARY, 
          {
            clusterIp: this.cluster?.bootStrapServers,
            refresh: false
           }
            );
        });

    });

    this.loader.loaded$
      .pipe(takeUntil(this.destoryed$),
        filter(data => data.context === 'SUMMARY_LIST')
      ).subscribe(d => {
        this.loaded = d.loaded;
      })
  }



  public brokerDetails(){
    this.router.navigate(['/brokers-details',this.cluster?.id]);
  }

  public topicsList(){
    this.router.navigate(['/topics', this.cluster?.id]);
  }
  public forceReload(){
    this.loader.change('SUMMARY_LIST', false);
    this.webSocketService.send(SocketTopics.SUMMARY, {
      clusterIp: this.cluster?.bootStrapServers,
      refresh: true
    });
  }
}
