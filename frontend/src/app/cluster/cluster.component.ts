import { Component, EventEmitter, Input, OnInit, Optional, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { AddNewComponent } from '../add-new/add-new.component';
import { ClusterStatus, KafkaCluster } from '../core/models/kafka-cluster.model';
import { AdminService } from '../core/services/admin.service';

@Component({
  selector: 'app-cluster',
  templateUrl: './cluster.component.html',
  styleUrls: ['./cluster.component.scss'],
})
export class ClusterComponent implements OnInit {
  public loading: boolean = false;
  @Input('item') cluster: KafkaCluster;
  @Output('delete') deleteEvent: EventEmitter<any> = new EventEmitter();
  constructor(private adminService: AdminService, private router: Router, public dialog: MatDialog) {
    this.cluster = new KafkaCluster();
   }

  ngOnInit(): void {
  }

  public refreshStatus() {
    this.loading = true;
    let cluster = this.adminService.findByid(this.cluster.id);
    this.adminService.healthCheck(this.cluster.id)
      .then(result => {
        if (result) {
          cluster.status = ClusterStatus.HEALTHY;
        }
        else {
          cluster.status = ClusterStatus.UNHEALTHY;
        }
        
      }).catch(error => {
        cluster.status = ClusterStatus.UNKOWN;
        
      }).finally(()=>{
        this.cluster.status = cluster.status;
        this.loading= false;
        this.adminService.update(cluster.id,cluster);
      })
  }

  public dashboard(): void {
    const cluster = this.adminService.findByid(this.cluster.id);

    if (cluster.status !== ClusterStatus.HEALTHY) {
      return;
    }
    this.adminService.setCurrent(this.cluster.id);
    this.router.navigate(['dashboard', this.cluster.id]);
  }

  public delete(): void {
    this.deleteEvent.emit();
  }

  public edit(): void {
    const dialogRef = this.dialog.open(AddNewComponent, {
      width: '50%',
      panelClass: 'kt-mat-dialog-container__wrapper',
      data: {edited: this.cluster}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
       this.cluster.id = result.id;
        this.cluster.bootStrapServers = result.bootStrapServers
      }
    });
  }


}
