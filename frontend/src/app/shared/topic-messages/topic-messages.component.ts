import { Component, Input, OnInit } from '@angular/core';
import { ReplaySubject } from 'rxjs';
import { Message } from '../../core/models/message.model';
import { Topic } from '../../core/models/topic.model';
import { KafkaMonitorService } from '../../core/services/kafka-monitor.service';
import * as FileSaver from 'file-saver';
import { MatDialog } from '@angular/material/dialog';
import { PayloadFilterComponent } from '../../payload-filter/payload-filter.component';
@Component({
  selector: 'app-topic-messages',
  templateUrl: './topic-messages.component.html',
  styleUrls: ['./topic-messages.component.scss']
})
export class TopicMessagesComponent implements OnInit {

  @Input('clusterId') clusterId: string = "";
  private destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  @Input('topic') topic?: Topic
  messages: Message[] = [];
  loaded: boolean = false;
  public from?: Date;
  public to?: Date;
  public count?: number;
  public filters: string[] = [];
  public filterModel: any = {};
  public selectedColumns: any[] = [];
  public columns: any[] = [];
  public allowDetails: boolean = false;
  constructor(private monitoringService: KafkaMonitorService, public dialog: MatDialog) { }



  ngOnDestroy(): void {
    this.destoryed$.complete();
  }

  ngOnInit(): void {
    const defaultSelectedColumns = [
      'timestamp',
      'partition',
      'offset',
      'key'
    ];
    this.selectedColumns = localStorage.getItem(`Selected_Columns_${this.topic?.name}`) != null 
      && (JSON.parse(localStorage.getItem(`Selected_Columns_${this.topic?.name}`) ?? "[]") as any[]).length > 0 ?
      JSON.parse(localStorage.getItem(`Selected_Columns_${this.topic?.name}`) ?? "[]") as any [] : 
      defaultSelectedColumns;
    this.search();
  }

 
  public changeSelectionColumns(event: any){
    localStorage.setItem(`Selected_Columns_${this.topic?.name}`,JSON.stringify(this.selectedColumns));
  }
  public exportExcel() {
    const msgs = this.messages
      .filter(msg => msg.message && msg.fromatedMessage)
      .map(msg => {
        return msg.fromatedMessage;
      });
    import("xlsx").then(xlsx => {
      const worksheet = xlsx.utils.json_to_sheet(msgs)
        
      const workbook = { Sheets: { 'data': worksheet }, SheetNames: ['data'] };
      const excelBuffer: any = xlsx.write(workbook, { bookType: 'xlsx', type: 'array' });
      this.saveAsExcelFile(excelBuffer,(this.topic?.name ?? new Date().getTime().toString()));
    });
  }

  saveAsExcelFile(buffer: any, fileName: string): void {
    let EXCEL_TYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8';
    let EXCEL_EXTENSION = '.xlsx';
    const data: Blob = new Blob([buffer], {
      type: EXCEL_TYPE
    });
    FileSaver.saveAs(data, fileName + '_export_' + new Date().getTime() + EXCEL_EXTENSION);
  }

  public search(){
    const start:number | undefined = this.from ? +this.from : undefined;
    const end: number | undefined = this.to ? +this.to : undefined;

    this.loaded = false;
    this.monitoringService.getMessages(this.topic?.name ?? "", this.clusterId, this.count, start,end)
      .then(data => {
        this.messages = data;
        alert(this.messages.length)
        this.flattenMessageObject();
        this.populateColumns();
        this.populateFilters();

        this.loaded = true;
      })
   
  }

  private populateFilters(){
    if (this.filterModel) {
      Object.keys(this.filterModel)
        .forEach(key => {
          if (key && this.filterModel[key]) {
            this.messages = this.messages.filter(d => d.fromatedMessage && d.fromatedMessage[key] == this.filterModel[key]);
          }
        })
    };

  }

  private flattenMessageObject(){
    this.messages.forEach(msg=> {
      if(msg.fromatedMessage){
        Object.keys(msg.fromatedMessage)
        .forEach(key=> {
         (msg as any)[key] = msg.fromatedMessage[key];
        })
      }
    })
  }

  populateColumns(){
    this.messages.forEach(msg=> {
      if(msg){
        Object.keys(msg).forEach(key=> {
          if (this.columns.findIndex(res => res == key) == -1) {
            if (key === 'fromatedMessage' && msg.fromatedMessage){
               Object.keys(msg.fromatedMessage)
               .forEach(msgKey=> {
                 this.columns.push(msgKey);
               })
            }
            else{
              this.columns.push(key);
            }
          }
        })
      }
    })
  }

  public moreFilters(){
    if(this.messages.length > 0){
      this.messages.forEach(msg=> {
        if(msg.fromatedMessage){
          Object.keys(msg.fromatedMessage)
          .forEach(key=> {
            if (this.filters.findIndex(res => res == key) == -1){
              this.filters.push(key);
            }
          })
        }
      })
      if (this.filters.length > 0){
        const dialogRef = this.dialog.open(PayloadFilterComponent, {
          disableClose: true,
          data: { filters: this.filters,topic: this.topic?.name, filterModel: this.filterModel },
          width: '60%',
          panelClass: 'kt-mat-dialog-container__wrapper'
        });
        dialogRef.afterClosed().subscribe(res => {
          if (!res) {
            return;
          }
          else{
            this.filterModel = res;
          }
       
      });
    }
  }
  }
}
