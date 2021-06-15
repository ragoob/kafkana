import { CdkDragDrop } from '@angular/cdk/drag-drop';
import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { AddNewComponent } from '../add-new/add-new.component';
import { ClusterStatus, KafkaCluster } from '../core/models/kafka-cluster.model';
import { AdminService } from '../core/services/admin.service';
import { trigger, transition, style, animate, query, stagger } from '@angular/animations';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  animations: [
    trigger('listAnimation', [
      transition('* => *', [
        query(':enter', [
          style({ opacity: 0 }),
          stagger(100, [
            animate('0.5s', style({ opacity: 1 }))
          ])
        ], { optional: true })
      ])
    ])
  ],
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  public loaded: boolean = false;
  public clusters: KafkaCluster[] = [];
  first = 0;
  rows = 5;
  constructor(private adminService: AdminService, public dialog: MatDialog
    ) { }
  
 
    
  ngOnInit(): void {
   this.load();
  }

  private load(){
  
    this.loaded = false;
    this.adminService.getAll()
      .then(data => {
        this.clusters = data;
        this.loaded = true;
      })
  }


  public addNewCluster(): void{
    const dialogRef = this.dialog.open(AddNewComponent, {
      width: '50%',
      panelClass: 'kt-mat-dialog-container__wrapper',
      data: {}
    });

    dialogRef.afterClosed().subscribe(result => {
       if(result){
         this.clusters.push(result)
         this.refreshStatus(result.id);
       }
    });
  }

  

  public delete(id: string){
    this.loaded = false;
     this.adminService.delete(id);
     const index = this.clusters.findIndex(c=> c.id == id);
     this.clusters.splice(index,1);
  }

  private update(cluster: KafkaCluster){
    this.adminService.update(cluster.id,cluster)
      .then(() => {
       const index =  this.clusters.findIndex(c=> c.id === cluster.id);
        this.clusters[index].status = cluster.status;
      })
  }

  public refreshStatus(clusterId: string){
    this.loaded = false;
    let cluster = this.adminService.findByid(clusterId);
    this.adminService.healthCheck(clusterId)
    .then(result=> {
       if(result){
         cluster.status = ClusterStatus.HEALTHY;
       }
       else{
         cluster.status = ClusterStatus.UNHEALTHY;
       }
       
    }).catch(error=>{
      cluster.status = ClusterStatus.UNKOWN;
     
    }).finally(()=> {
      this.update(cluster);
    })
  }

  public drop(event: CdkDragDrop<any>) {
    this.clusters[event.previousContainer.data.index] = event.container.data.item;
    this.clusters[event.container.data.index] = event.previousContainer.data.item;
    event.currentIndex = 0;
  }

}
