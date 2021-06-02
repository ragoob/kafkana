import { Component, OnInit } from '@angular/core';
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
  constructor(private adminService: AdminService) { }
  
  ngOnInit(): void {
    this.adminService.getAll()
    .then(data=> {
      this.clusters = data;
    })
  }

  public addNewCluster(): void{
    alert("Under Dev")
  }

}
