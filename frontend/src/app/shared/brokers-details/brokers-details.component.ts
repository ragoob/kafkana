import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReplaySubject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Brokers } from '../../core/models/brokers.model';
import { KafkaAdminService } from '../../core/services/kafka-admin.service';

@Component({
  selector: 'app-brokers-details',
  templateUrl: './brokers-details.component.html',
  styleUrls: ['./brokers-details.component.scss']
})
export class BrokersDetailsComponent implements OnInit, OnDestroy{
  clusterId: string = "";
  private destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  public brokers: Brokers[] = [];
  active: number = 0;
  constructor(private kafkaAdminService: KafkaAdminService,private route: ActivatedRoute) { }
  ngOnDestroy(): void {
    this.destoryed$.complete();
  }

  ngOnInit(): void {
    this.route.params.
      pipe(takeUntil(this.destoryed$))
      .subscribe(params => {
        this.clusterId = params.id;
        this.loadBrokers(params.id);
       
      })
  }

  private loadBrokers(clusterId: string) {
    this.kafkaAdminService.getNodes(clusterId)
      .then(data => {
        if(data.length){
          this.active = data[0].id;
          this.brokers = data;
        }
       
      })
  }

}
