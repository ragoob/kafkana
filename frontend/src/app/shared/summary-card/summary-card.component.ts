import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ReplaySubject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { LoadingService } from '../../core/services/loading.service';

@Component({
  selector: 'app-summary-card',
  templateUrl: './summary-card.component.html',
  styleUrls: ['./summary-card.component.scss']
})
export class SummaryCardComponent implements OnInit {
  private destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  @Input('value') value?: number;
  @Input('title') title: string = "";
  @Input('icon') icon?: string;
  @Input('timeStamp') timeStamp?: Date
  @Input('isPercentage') isPercentage : boolean = false;
  @Output('refresh') refresh: EventEmitter<any> = new EventEmitter();
  public loaded: boolean = false;
  constructor(private loader: LoadingService) { }

  ngOnInit(): void {
    this.loader.loaded$
      .pipe(takeUntil(this.destoryed$),
        filter(data => data.context === 'SUMMARY_LIST')
      ).subscribe(d => {
        this.loaded = d.loaded;
      })
  }

  public forceReload(){
    if(this.refresh){
      this.refresh.emit();
    }
  }

 


}
