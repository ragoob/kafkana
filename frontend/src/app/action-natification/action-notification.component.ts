// Angular
import { Component, Inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { MAT_SNACK_BAR_DATA} from '@angular/material/snack-bar';
// RxJS
import { delay } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
	selector: 'kt-action-natification',
	templateUrl: './action-notification.component.html',
	changeDetection: ChangeDetectionStrategy.Default

})
export class ActionNotificationComponent implements OnInit {
	/**
	 * Component constructor
	 *
	 * @param data: any
	 */
	constructor(@Inject(MAT_SNACK_BAR_DATA) public data: any) { }


	ngOnInit() {
		if (!this.data.showUndoButton || (this.data.undoButtonDuration >= this.data.duration)) {
			return;
		}

		this.delayForUndoButton(this.data.undoButtonDuration).subscribe(() => {
			this.data.showUndoButton = false;
		});
	}

	delayForUndoButton(timeToDelay: any) {
		return of('').pipe(delay(timeToDelay));
	}

	
	onDismissWithAction() {
		this.data.snackBar.dismiss();
	}


  	public onDismiss() {
		this.data.snackBar.dismiss();
	}
}
