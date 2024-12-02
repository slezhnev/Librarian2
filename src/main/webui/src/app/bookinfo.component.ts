import { Component, OnInit } from '@angular/core';
import { DownloadService } from "./download.service"
import {Book, Author} from "./models"

@Component({
	selector: 'bookinfo',
	template: '
	@for(author of book.authors; track author.authorId) {
	    <author author={{ author }}/><br/>
	}
	<br/>
	{{ book.title }} 
	@if (book.serieName.trim().length != 0) {
		({{book.serieName}} - {{book.numInSerie}})
	} 
	<br/>
	<button (click)="downloadBook()">Download book</button>
	'
})

export class BookInfo implements OnInit {
	
	constructor(private ds : DownloadService) {
	}
	
	@Input book : Book;
	
	downloadBook() {
		this.ds.downloadBook(book.bookId, book.libraryId);	
	}
}