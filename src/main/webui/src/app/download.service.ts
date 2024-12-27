import { inject, Injectable } from '@angular/core';
import { UserWrapper } from './user.service';

@Injectable({
	providedIn: 'root',
})
export class DownloadService {

	userWrapper = inject(UserWrapper)

	downloadBook(bookId: number, downloadType: number) {
		window.open("/download/" +bookId + "/" + downloadType + "/" + this.userWrapper.userId, "_blank");		
	}
	
}