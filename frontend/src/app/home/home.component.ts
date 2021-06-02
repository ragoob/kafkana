import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
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
  constructor(private adminService: AdminService, public dialog: MatDialog) { }
  
  ngOnInit(): void {
    this.adminService.getAll()
    .then(data=> {
      this.clusters = data;
    })
  }

  public addNewCluster(): void{
    const dialogRef = this.dialog.open(AddNewComponent, {
      width: '250px',
      data: {}
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }

}
