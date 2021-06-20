import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { KafkaAdminService } from '../../core/services/kafka-admin.service';
import { LayoutUtilsService, MessageType } from '../../core/services/layout-utils.service';

@Component({
  selector: 'app-topic-create',
  templateUrl: './topic-create.component.html',
  styleUrls: ['./topic-create.component.scss']
})
export class TopicCreateComponent implements OnInit {
  public addNewForm: FormGroup;
  public isSubmit: boolean = false;
  constructor(public dialogRef: MatDialogRef<TopicCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private kafkaAdminService: KafkaAdminService,
    private layoutUtilsService: LayoutUtilsService
    ) {
    this.addNewForm = this.formBuilder.group({
      topicName: ['', Validators.required],
      partitions: [1, Validators.required],
      replication: [1, Validators.required],
      configurations: []
    });
    }

  ngOnInit(): void {
  }


  close(result: any): void {
    this.isSubmit = false;
    Object.keys(this.addNewForm.controls).forEach(key => {
      this.addNewForm.get(key)?.markAsUntouched();
    });
    this.dialogRef.close(result);
  }

  public save(){
    this.isSubmit = true;
    Object.keys(this.addNewForm.controls).forEach(key => {
      this.addNewForm.get(key)?.markAsDirty();
    });

   

    this.kafkaAdminService.createTopic(this.addNewForm.value, this.data.clusterId)
    .then(()=> {
      this.close(this.addNewForm.value);
      const message = `Topic has been created successfully.`;
      this.layoutUtilsService.showActionNotification(message, MessageType.Create, 5000, true, false, undefined, 'top');
    }).catch((error)=>{
      this.layoutUtilsService.showActionNotification(error.error, MessageType.Create, 5000, true, false, undefined, 'top');
    })
    
  }

}
