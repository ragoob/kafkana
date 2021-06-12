import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReplaySubject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { ClusterSummary } from '../../core/models/cluster-summary.model';
import { KafkaMonitorService } from '../../core/services/kafka-monitor.service';
import { LoadingService } from '../../core/services/loading.service';

@Component({
  selector: 'app-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.scss']
})
export class SummaryComponent implements OnInit ,OnDestroy{
  clusterId: string = "";
  private destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  public summary?: ClusterSummary | null
  constructor(private monitoringService: KafkaMonitorService, 
    private route: ActivatedRoute,private router: Router,
    private loader: LoadingService
    ) { }

  ngOnDestroy(): void {
    this.destoryed$.complete();
  }

  ngOnInit(): void {
    this.route.params.
    pipe(takeUntil(this.destoryed$))
    .subscribe(params=> {
      this.clusterId = params.id;
      this.loadSummary(params.id);
    });

   
  }

  public loadSummary(clusterId: string,refresh: boolean =false){
    this.loader.change('SUMMARY_LIST', false);
    this.monitoringService.getSummary(clusterId, refresh)
    .then(data=> {
      this.summary = data;
      this.loader.change('SUMMARY_LIST', true);
    }).catch(error=> {
      this.summary = null;
      this.loader.change('SUMMARY_LIST', true);
    })
  }

  public brokerDetails(){
    this.router.navigate(['/brokers-details',this.clusterId]);
  }

  public topicsList(){
    this.router.navigate(['/topics',this.clusterId]);
  }
}
