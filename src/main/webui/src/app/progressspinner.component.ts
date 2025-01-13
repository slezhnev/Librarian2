import { Component, inject } from '@angular/core';
import {
    MatDialog,
    MatDialogRef,
} from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
    selector: 'progress-spinner',
    templateUrl: './progressspinner.component.html',
    styleUrls: ['./progressspinner.component.css']
})
export class ProgressSpinner {
    readonly dialog = inject(MatDialog);

    openDialog() {        
        this.dialog.open(ProgressSpinnerDialogComponent, {
            panelClass: 'transparent',
            disableClose: true
        });
    }

    closeDialog() {
        this.dialog.closeAll();
    }
}


@Component({
    selector: 'progressspinner-dialog',
    templateUrl: './progressspinner-dialog.html',
    styleUrls: ['./progressspinner-dialog.css'],
    imports: [MatProgressSpinnerModule]
})
export class ProgressSpinnerDialogComponent {
    readonly dialogRef = inject(MatDialogRef<ProgressSpinnerDialogComponent>);
}