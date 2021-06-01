import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReplaySubject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ClusterSummary } from '../../core/models/cluster-summary.model';
import { KafkaMonitorService } from '../../core/services/kafka-monitor.service';

@Component({
  selector: 'app-summary',
  templateUrl: './summary.component.html',
  styleUrls: ['./summary.component.scss']
})
export class SummaryComponent implements OnInit ,OnDestroy{
  clusterId: string = "";
  private destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  public summary?: ClusterSummary
  constructor(private monitoringService: KafkaMonitorService, private route: ActivatedRoute,private router: Router) { }

  ngOnDestroy(): void {
    this.destoryed$.complete();
  }

  ngOnInit(): void {
    this.route.params.
    pipe(takeUntil(this.destoryed$))
    .subscribe(params=> {
      this.clusterId = params.id;
      this.loadSummary(params.id);
    })
  }

  private loadSummary(clusterId: string){
    this.monitoringService.getSummary(clusterId)
    .then(data=> {
      this.summary = data;
    })
  }

  public brokerDetails(){
    this.router.navigate(['/brokers-details',this.clusterId]);
  }

}
