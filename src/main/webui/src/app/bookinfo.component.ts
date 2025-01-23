import { Component, inject, Input, Output, EventEmitter } from '@angular/core';
import { DownloadService } from "./services/download.service"
import { Book, Author } from "./models/models"
import { AuthorLink } from './authorlink.component';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatRadioModule } from '@angular/material/radio';
import { FormsModule } from '@angular/forms';


@Component({
	selector: 'bookinfo',
	imports: [AuthorLink, MatButtonModule, MatCardModule, MatRadioModule, FormsModule],
	templateUrl: './bookinfo.component.html',
})

export class BookInfo {

	ds = inject(DownloadService)

	@Input() book: Book | null = null;
	@Output() switchToAuthorEvent = new EventEmitter<Author>();
	@Output() switchToSerieEvent = new EventEmitter<string>();

	downloadType = 'fb2.zip';

	downloadBook() {
		if (this.book && this.book.bookId) {
			if (this.downloadType == "fb2") {
				this.ds.downloadBook(this.book.bookId, 1);
			} else {
				this.ds.downloadBook(this.book.bookId, 2);
			}
		}
	}

	switchToAuthor(author: Author) {
		// Send event higher
		this.switchToAuthorEvent.emit(author);
	}

	switchToSerie() {
		if (this.book) {
			this.switchToSerieEvent.emit(this.book.serieName);
		}
	}
}