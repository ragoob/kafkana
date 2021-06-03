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
export class AddNewComponent  implements OnInit{
  public addNewForm: FormGroup;
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
   

    
  }

  close(): void {
    Object.keys(this.addNewForm.controls).forEach(key => {
      this.addNewForm.get(key)?.markAsUntouched();
    });
    this.dialogRef.close();
  }

  save(): void{
    Object.keys(this.addNewForm.controls).forEach(key => {
      this.addNewForm.get(key)?.markAsDirty();
    });
    if(this.addNewForm.valid){
      this.adminService.create(this.addNewForm.value)
        .then(() => {
          this.close();
        })
    }
    
  }

}
