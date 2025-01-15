import { Component, inject, OnInit } from '@angular/core';
import {
    MatDialog,
    MatDialogRef,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    MatDialogTitle,
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { BooksProcessingStatusService } from './booksstatus.service'
import { catchError } from 'rxjs';

@Component({
    selector: 'booksstatus',
    templateUrl: './booksstatus.component.html'
})
export class BooksUpdateStatusDialog {
    readonly dialog = inject(MatDialog);

    openDialog() {
        this.dialog.open(BooksStatusDialog);
    }
}

@Component({
    selector: 'statusdialog',
    templateUrl: './statusdialog.component.html',
    styleUrl: './statusdialog.component.css',
    imports: [
        MatButtonModule,
        MatDialogTitle,
        MatDialogContent,
        MatDialogActions,
        MatDialogClose,
    ],
})
export class BooksStatusDialog implements OnInit {
    libraryStatus = "Loading...";
    filesStatus = ""
    booksStatus = ""

    readonly statusService: BooksProcessingStatusService = inject(BooksProcessingStatusService)

    cronJob: any;

    constructor(public dialogRef: MatDialogRef<BooksStatusDialog>) { }

    ngOnInit() {
        this.dialogRef.afterOpened().subscribe(() => {
            this.fetchStatus();
            this.startCronJob();
        })

        this.dialogRef.afterClosed().subscribe(() => {
            clearInterval(this.cronJob)
        })
    }

    fetchStatus() {
        this.statusService.getStatus().pipe(
            catchError(error => {
                console.error('Cannot get book processing status');
                throw new Error('Cannot get book processing status');
            })
        ).subscribe({
            next: data => {
                if (data.checkinginProgress) {
                    this.libraryStatus = "Checking for new books, please wait...";
                    this.filesStatus = ""
                    this.booksStatus = ""
                } else
                    if (data.totalArcsToProcess && data.totalArcsToProcess > 0) {
                        this.libraryStatus = "Processing " + data.currentLibrary;
                        this.filesStatus = "Processing " + data.currentArcName + " (" + data.currentArcsToProcess + " of " + data.totalArcsToProcess + ")";
                        if (data.currentFileToProcess && data.totalFilesToProcess && data.currentFileToProcess > data.totalFilesToProcess) {
                            this.booksStatus = "Saving books in progress...";
                        } else {
                            this.booksStatus = "Processing in archive " + data.currentFileToProcess + " of total " + data.totalFilesToProcess;
                        }
                    } else {
                        this.libraryStatus = "No new book in processing at this moment";
                        this.filesStatus = ""
                        this.booksStatus = ""
                            }
            },
            error: error => {
                this.libraryStatus = error.message;
                this.filesStatus = ""
                this.booksStatus = ""
            }
        })
    }

    startCronJob() {
        this.cronJob = setInterval(() => {
            this.fetchStatus();
        }, 10000); // Fetch status every 30 seconds
    }
}