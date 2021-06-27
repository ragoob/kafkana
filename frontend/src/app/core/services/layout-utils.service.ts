// Angular
import { Injectable,EventEmitter } from '@angular/core';
import {MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BehaviorSubject } from 'rxjs';
 import { ActionNotificationComponent } from '../../action-natification/action-notification.component';
import { ConfirmDeleteDialogComponent } from '../../confirm-delete-dialog/confirm-delete-dialog.component';
// Partials for CRUD

export enum MessageType {
	Create,
	Read,
	Update,
	Delete
}

@Injectable()
export class LayoutUtilsService {
	/**
	 * Service constructor
	 *
	 * @param snackBar: MatSnackBar
	 * @param dialog: MatDialog
	 */
	constructor(private snackBar: MatSnackBar,
		private dialog: MatDialog) { }

	/**
	 * Showing (Mat-Snackbar) Notification
	 *
	 * @param message: string
	 * @param type: MessageType
	 * @param duration: number
	 * @param showCloseButton: boolean
	 * @param showUndoButton: boolean
	 * @param undoButtonDuration: number
	 * @param verticalPosition: 'top' | 'bottom' = 'top'
	 */
	showActionNotification(
		_message: string,
		_type: MessageType = MessageType.Create,
		_duration: number = 10000,
		_showCloseButton: boolean = true,
		_showUndoButton: boolean = false,
		_undoButtonDuration: number = 3000,
		_verticalPosition: 'top' | 'bottom' = 'bottom'
	) {
		const _data = {
			message: _message,
			snackBar: this.snackBar,
			showCloseButton: _showCloseButton,
			showUndoButton: _showUndoButton,
			undoButtonDuration: _undoButtonDuration,
			verticalPosition: _verticalPosition,
			type: _type,
			action: 'Undo'
		};
		return this.snackBar.openFromComponent(ActionNotificationComponent, {
			duration: _duration,
			data: _data,
			verticalPosition: _verticalPosition
		});
	}

	/**
	 * Showing Confirmation (Mat-Dialog) before Entity Removing
	 *
	 * @param title: stirng
	 * @param description: stirng
	 * @param waitDesciption: string
	 */
	deleteElement(title: string = '', description: string = '', waitDesciption: string = '', waitUntil?: BehaviorSubject<boolean>, panelClass: string = '' ) {
		return this.dialog.open(ConfirmDeleteDialogComponent, {
			data: { title, description, waitDesciption, waitUntil},
			width: '440px',
			panelClass: panelClass
		});
	}


	
	
}
