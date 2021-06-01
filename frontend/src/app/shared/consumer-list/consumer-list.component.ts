import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ConfirmationService } from 'primeng/api';
import { ReplaySubject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Consumer } from '../../core/models/consumer-model';
import { KafkaAdminService } from '../../core/services/kafka-admin.service';
import { KafkaMonitorService } from '../../core/services/kafka-monitor.service';

@Component({
  selector: 'app-consumer-list',
  templateUrl: './consumer-list.component.html',
  styleUrls: ['./consumer-list.component.scss']
})
export class ConsumerListComponent implements OnInit , OnDestroy{
  clusterId: string = "";
  first = 0;
  rows = 5;
  public consumers: Consumer[] = [];
  private destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  constructor(private monitoringService: KafkaMonitorService, 
    private route: ActivatedRoute) { }
  ngOnDestroy(): void {
    this.destoryed$.complete();
  }

  ngOnInit(): void {
    this.route.params.
      pipe(takeUntil(this.destoryed$))
      .subscribe(params => {
        this.clusterId = params.id;
        this.loadConsumers(params.id);
      })
  }

  private loadConsumers(clusterId: string) {
    this.monitoringService.getConsumers(clusterId)
      .then(data => {
        this.consumers = data;
      })
  }
}
