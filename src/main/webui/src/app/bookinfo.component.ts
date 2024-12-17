import { Component, inject, Input } from '@angular/core';
import { DownloadService } from "./download.service"
import { Book } from "./models"
import { AuthorLink } from './authorlink.component';

@Component({
	selector: 'bookinfo',
	imports: [AuthorLink],
	templateUrl: './bookinfo.component.html',
})

export class BookInfo {
	
	ds = inject(DownloadService)

	@Input()
	book: Book|null = null;
	
	downloadBook() {
		if (this.book && this.book.bookId && this.book.libraryId) {
			this.ds.downloadBook(this.book.bookId, this.book.libraryId);
		}	
	}
}