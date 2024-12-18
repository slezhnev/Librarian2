import { Component, inject, Input, Output, EventEmitter } from '@angular/core';
import { DownloadService } from "./download.service"
import { Book, Author } from "./models"
import { AuthorLink } from './authorlink.component';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

@Component({
	selector: 'bookinfo',
	imports: [AuthorLink, MatButtonModule, MatCardModule],
	templateUrl: './bookinfo.component.html',
})

export class BookInfo {

	ds = inject(DownloadService)

	@Input() book: Book | null = null;
	@Output() switchToAuthorEvent = new EventEmitter<Author>();
	@Output() switchToSerieEvent = new EventEmitter<string>();

	downloadBook() {
		if (this.book && this.book.bookId && this.book.libraryId) {
			this.ds.downloadBook(this.book.bookId, this.book.libraryId);
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