import { Component, Input } from '@angular/core';
import { Book, Author} from "./models"

@Component({
	selector: 'authorlink',
	templateUrl: './authorlink.component.html',
})

export class AuthorLink {
	
	@Input() author : Author|null = null;

	switchToAuthor() {
		// Do nothing at the moment - but we should send event to search part aftewards
		if (this.author) {
			console.info("Author " + this.author?.authorId + " selected - should be loaded after")
		} else {
			console.error("No author - it is quite strange")
		}
	}
	
}