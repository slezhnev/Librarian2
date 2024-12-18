import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Book, Author} from "./models"
import {MatButtonModule} from '@angular/material/button';

@Component({
	selector: 'authorlink',
	templateUrl: './authorlink.component.html',
	imports: [MatButtonModule],
})

export class AuthorLink {
	
	@Input() author : Author|null = null;
	@Output() switchToAuthorEvent = new EventEmitter<Author>();

	switchToAuthor() {
		if (this.author) {
			console.debug(" Trying to switch to author " + this.author?.authorId)
			this.switchToAuthorEvent.emit(this.author);
		} else {
			console.error("No author - it is quite strange")
		}
	}
	
}