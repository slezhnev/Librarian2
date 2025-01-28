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
import { catchError } from 'rxjs';
import { DownloadService } from './services/download.service';

@Component({
    selector: 'booksdownload',
    templateUrl: './booksdownload.component.html'
})
export class BooksDownloadDialog {
    readonly dialog = inject(MatDialog);

    openDialog() {
        this.dialog.open(BooksDownloadingDialog);
    }
}

@Component({
    selector: 'downloaddialog',
    templateUrl: './downloaddialog.component.html',
    styleUrl: './downloaddialog.component.css',
    imports: [
        MatButtonModule,
        MatDialogTitle,
        MatDialogContent,
        MatDialogActions,
        MatDialogClose,
    ],
})
export class BooksDownloadingDialog implements OnInit {
    overallStatus = "Loading...";
    status = ""

    readonly downloadService: DownloadService = inject(DownloadService)

    cronJob: any;

    constructor(public dialogRef: MatDialogRef<BooksDownloadDialog>) { }

    booksToDownload: number[] = [];
    currentBookToProcess = -1;

    ngOnInit() {
        this.dialogRef.afterOpened().subscribe(() => {
            this.getNumberToDownload();
        })

        this.dialogRef.afterClosed().subscribe(() => {
            clearInterval(this.cronJob)
        })
    }

    getNumberToDownload() {
        this.downloadService.getBooksToDownload().pipe(
            catchError(error => {
                console.error('Cannot get books to download');
                throw new Error(error);
            })
        ).subscribe({
            next: data => {
                this.booksToDownload = data;
                this.currentBookToProcess = 0;
                if (this.booksToDownload.length > 0) {
                    this.startToPrepareToDownload();
                    this.startCronJob();
                } else {
                    this.updateStateLabels()
                    this.currentBookToProcess = -1;
                    clearInterval(this.cronJob);
                }
            },
            error: error => {
                this.booksToDownload = [];
                this.currentBookToProcess = -1;
                this.overallStatus = "Cannot get books to download"
                this.status = error;
                clearInterval(this.cronJob);
            }
        })
    }

    startToPrepareToDownload() {
        this.overallStatus = "Books preparation started..."
        this.status = ""
        this.booksToDownload.forEach((book) => {
            this.downloadService.prepareToDownload(book, 2).pipe(
                catchError(error => {
                    console.error('Cannot prepare book to download - ' + book);
                    throw new Error(error);
                })
            ).subscribe({
                next: data => {
                    // Do nothing - we can got any code there, it is normal
                },
                error: error => {
                    this.booksToDownload = [];
                    this.currentBookToProcess = -1;
                    this.overallStatus = "Cannot get books to download"
                    this.status = error;
                    clearInterval(this.cronJob);
                }
            })
        })
    }

    updateStateLabels() {
        if (this.booksToDownload.length > 0 && this.currentBookToProcess > -1) {
            let currentLabel = this.currentBookToProcess + 1
            this.overallStatus = "Processing " + currentLabel + " of " + this.booksToDownload.length
        } else {
            this.overallStatus = "No books to download"
            this.status = ""
        }
    }

    downloadType: number = 2;

    updateBookStatus() {        
        this.updateStateLabels()
        if (this.currentBookToProcess > -1 && this.currentBookToProcess < this.booksToDownload.length) {
            this.downloadService.prepareToDownload(this.booksToDownload[this.currentBookToProcess], this.downloadType).pipe(
                catchError(error => {
                    console.error('Cannot prepare book to download - ' + this.currentBookToProcess);
                    throw new Error(error);
                })
            ).subscribe({
                next: data => {
                    switch(data) {
                        case 0: {
                            this.status = "Preparation started..."
                            break;
                        }
                        case 1: {
                            this.status = "Preparation in progress..."
                            break;
                        }
                        case 2: {
                            this.status = ""
                            this.downloadService.downloadPreparedBook(this.booksToDownload[this.currentBookToProcess], this.downloadType);
                            this.currentBookToProcess = this.currentBookToProcess + 1;
                            if (this.currentBookToProcess >= this.booksToDownload.length) {
                                this.status = "All books was downloaded"
                                clearInterval(this.cronJob);
                            } else {
                                this.updateStateLabels()
                            }
                            break;
                        }
                        default: {

                        }
                    }
                },
                error: error => {
                    this.booksToDownload = [];
                    this.currentBookToProcess = -1;
                    this.overallStatus = "Cannot get books to download"
                    this.status = error;
                    clearInterval(this.cronJob);
                }
            })
        }
    }

    startCronJob() {
        this.cronJob = setInterval(() => {
            this.updateBookStatus();
        }, 10000); // Fetch status every 30 seconds
    }
}