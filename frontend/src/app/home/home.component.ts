import { THIS_EXPR } from '@angular/compiler/src/output/output_ast';
import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { AddNewComponent } from '../add-new/add-new.component';
import { ClusterStatus, KafkaCluster } from '../core/models/kafka-cluster.model';
import { AdminService } from '../core/services/admin.service';
@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  public loaded: boolean = false;
  public clusters: KafkaCluster[] = [];
  first = 0;
  rows = 5;
  constructor(private adminService: AdminService, public dialog: MatDialog,
    private router: Router
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
         this.refreshStatus(result.clusterId);
       }
    });
  }

  public dashboard(id: string) : void{
    const cluster = this.adminService.findByid(id);
  
    if(cluster.status !== ClusterStatus.HEALTHY){
      return;
    }
    this.adminService.setCurrent(id);
    this.router.navigate(['dashboard',id]);
  }

  public delete(id: string){
    this.loaded = false;
     this.adminService.delete(id);
     this.load();
  }

  private update(cluster: KafkaCluster){
    this.adminService.update(cluster)
      .then(d => {
        this.load();
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
       this.update(cluster);

      
    }).catch(error=>{
      cluster.status = ClusterStatus.UNKOWN;
      this.update(cluster);
    })
  }
}
