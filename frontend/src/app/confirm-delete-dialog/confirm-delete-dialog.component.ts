import { Component, EventEmitter, Inject, OnDestroy } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { BehaviorSubject, ReplaySubject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-confirm-delete-dialog',
  templateUrl: './confirm-delete-dialog.component.html',
  styleUrls: ['./confirm-delete-dialog.component.scss']
})
export class ConfirmDeleteDialogComponent implements OnDestroy {
  onDelete = new EventEmitter();
  public destoryed$: ReplaySubject<any> = new ReplaySubject(1);
  viewLoading = false;
  constructor(
    public dialogRef: MatDialogRef<ConfirmDeleteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }
  ngOnDestroy(): void {
    this.destoryed$.complete();
  }


  onNoClick(): void {
    this.dialogRef.close();
  }

  onYesClick(): void {
    this.viewLoading = true;
    this.onDelete.emit();
    if(this.data && this.data.waitUntil){
      (this.data.waitUntil as BehaviorSubject<boolean>)
      .pipe(filter(res=> res == true),takeUntil(this.destoryed$))
      .subscribe(()=> {
        this.dialogRef.close();
      })
    }
    else{
      this.dialogRef.close();
    }
    
  }

}
