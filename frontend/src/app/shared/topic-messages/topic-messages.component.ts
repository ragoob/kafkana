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
  public Filters: string[] = [];
  public filterModel: any = {};
  constructor(private monitoringService: KafkaMonitorService, public dialog: MatDialog) { }



  ngOnDestroy(): void {
    this.destoryed$.complete();
  }

  ngOnInit(): void {
    this.loadmessages(this.topic?.name ?? "", this.clusterId);
  }

  private loadmessages(topicName: string, clusterId: string) {
    this.loaded = false;
    this.monitoringService.getMessages(topicName, clusterId)
      .then(data => {
        this.messages = data;
        this.loaded = true;
      })
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
        if(this.filterModel){
          Object.keys(this.filterModel)
          .forEach(key=> {
            if(key && this.filterModel[key]){
              this.messages = this.messages.filter(d=> d.fromatedMessage &&  d.fromatedMessage[key] == this.filterModel[key]);
            }
          })
        }
        this.loaded = true;
      })
   
  }

  public moreFilters(){
    if(this.messages.length > 0){
      this.messages.forEach(msg=> {
        if(msg.fromatedMessage){
          Object.keys(msg.fromatedMessage)
          .forEach(key=> {
            if (this.Filters.findIndex(res => res == key) == -1){
              this.Filters.push(key);
            }
          })
        }
      })
      if (this.Filters.length > 0){
        const dialogRef = this.dialog.open(PayloadFilterComponent, {
          disableClose: true,
          data: { filters: this.Filters,topic: this.topic?.name, filterModel: this.filterModel },
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
