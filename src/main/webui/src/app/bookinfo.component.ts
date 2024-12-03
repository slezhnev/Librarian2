import { Component, OnInit } from '@angular/core';
import { DownloadService } from "./download.service"
import {Book, Author} from "./models"

@Component({
	selector: 'bookinfo',
	templateUrl: './bookinfo.component.html',
})

export class BookInfo implements OnInit {
	
	constructor(private ds : DownloadService) {
	}
	
	@Input book : Book;
	
	downloadBook() {
		this.ds.downloadBook(book.bookId, book.libraryId);	
	}
}