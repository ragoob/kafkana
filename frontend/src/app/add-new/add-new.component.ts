import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { AdminService } from '../core/services/admin.service';
import { ThrowStmt } from '@angular/compiler';

@Component({
  selector: 'app-add-new',
  templateUrl: './add-new.component.html',
  styleUrls: ['./add-new.component.scss']
})
export class AddNewComponent implements OnInit {
  public addNewForm: FormGroup;
  public isSubmit: boolean =false;
  constructor(
    public dialogRef: MatDialogRef<AddNewComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private adminService: AdminService
    
    ) { 

    this.addNewForm = this.formBuilder.group({
      id: ['', Validators.required],
      bootStrapServers: ['', Validators.required]
    });
    
    }
  ngOnInit(): void {
    if(this.data && this.data.edited){
      this.addNewForm.controls["id"].setValue(this.data.edited.id);
      this.addNewForm.controls["bootStrapServers"].setValue(this.data.edited.bootStrapServers);
    }
  }
  

  close(result: any): void {
    this.isSubmit = false;
    Object.keys(this.addNewForm.controls).forEach(key => {
      this.addNewForm.get(key)?.markAsUntouched();
    });
    this.dialogRef.close(result);
  }

  save(): void{
    this.isSubmit = true;
    Object.keys(this.addNewForm.controls).forEach(key => {
      this.addNewForm.get(key)?.markAsDirty();
    });
    if(this.addNewForm.valid){
      if (this.data && this.data.edited){
        this.adminService.update(this.data.edited.id,this.addNewForm.value)
          .then(() => {
            this.close(this.addNewForm.value);
          })
      }
      
      else{
        this.adminService.create(this.addNewForm.value)
          .then(() => {
            this.close(this.addNewForm.value);
          })
      }
    }
    
  }

}
