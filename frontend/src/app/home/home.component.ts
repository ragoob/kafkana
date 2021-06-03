import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { AddNewComponent } from '../add-new/add-new.component';
import { KafkaCluster } from '../core/models/kafka-cluster.model';
import { AdminService } from '../core/services/admin.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
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
    this.adminService.getAll()
      .then(data => {
        this.clusters = data;
      })
  }

  public addNewCluster(): void{
    const dialogRef = this.dialog.open(AddNewComponent, {
      width: '50%',
      position: {
        top: '0px',
        left: '0px'
      },
      data: {}
    });

    dialogRef.afterClosed().subscribe(result => {
       this.load();
    });
  }

  public dashboard(id: string) : void{
    this.adminService.setCurrent(id);
    this.router.navigate(['dashboard',id]);
  }

  public delete(id: string){
     this.adminService.delete(id);
     this.load();
  }
}
