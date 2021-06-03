import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationStart, Router } from '@angular/router';
import { ReplaySubject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AdminService } from '../core/services/admin.service';

@Component({
  selector: 'app-side-bar',
  templateUrl: './side-bar.component.html',
  styleUrls: ['./side-bar.component.scss']
})
export class SideBarComponent{
  public clusterId: string = "";
  constructor(private adminService: AdminService) { 
    this.clusterId = this.adminService.getCurrent();
  }
  

}
