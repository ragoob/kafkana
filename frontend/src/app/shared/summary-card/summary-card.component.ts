import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-summary-card',
  templateUrl: './summary-card.component.html',
  styleUrls: ['./summary-card.component.scss']
})
export class SummaryCardComponent implements OnInit {
  @Input('value') value?: number;
  @Input('title') title: string = "";
  @Input('icon') icon?: string;
  @Input('timeStamp') timeStamp?: Date
  constructor() { }

  ngOnInit(): void {
  }

 


}
