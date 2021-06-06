import { Component, Inject, Input, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-payload-filter',
  templateUrl: './payload-filter.component.html',
  styleUrls: ['./payload-filter.component.scss']
})
export class PayloadFilterComponent implements OnInit {
  public topic: string = "";
  public filters: [] = [];
  public model: any = {};
  constructor(
    public dialogRef: MatDialogRef<PayloadFilterComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) { }

  ngOnInit(): void {
    this.filters = this.data.filters;
    this.topic = this.data.topic;
    this.model = this.data.filterModel;
  }

  applyFilter(): void {
  
    this.dialogRef.close(this.model);
  }

}
