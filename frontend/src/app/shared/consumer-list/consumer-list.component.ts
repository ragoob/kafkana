import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReplaySubject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { Consumer } from '../../core/models/consumer-model';
import { KafkaMonitorService } from '../../core/services/kafka-monitor.service';
import { LoadingService } from '../../core/services/loading.service';

@Component({
  selector: 'app-consumer-list',
  templateUrl: './consumer-list.component.html',
  styleUrls: ['./consumer-list.component.scss']
})
export class ConsumerListComponent implements OnInit , OnDestroy{
  clusterId: string = "";
  first = 0;
  rows = 5;
  public loaded: boolean = false;
  public consumers: Consumer[] = [];
  private destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  constructor(private monitoringService: KafkaMonitorService, 
    private route: ActivatedRoute,
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
        this.loadConsumers(params.id);
      });

      this.loader.loaded$
      .pipe(takeUntil(this.destoryed$),
       filter(data=> data.context === 'CONSUMER_LIST')
      ).subscribe(d=> {
        this.loaded = d.loaded;
      })
  }

  public loadConsumers(clusterId: string,refresh: boolean = false) {
    this.loader.change('CONSUMER_LIST',false);
    this.monitoringService.getConsumers(clusterId, refresh)
      .then(data => {
        this.consumers = data;
        this.loader.change('CONSUMER_LIST',true);
      }).catch(error=> {
        this.consumers = [];
        this.loader.change('CONSUMER_LIST', true);
      })
  }
}
