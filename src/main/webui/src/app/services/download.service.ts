import { inject, Injectable } from '@angular/core';
import { UserWrapper } from '../user.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
	providedIn: 'root',
})
export class DownloadService {

	readonly userWrapper: UserWrapper = inject(UserWrapper)

	readonly http: HttpClient = inject(HttpClient)

	downloadBook(bookId: number, downloadType: number) {
		window.open("/download/book/" + bookId + "/" + downloadType + "/" + this.userWrapper.userId, "_blank");
	}

	getBooksToDownload(): Observable<number[]> {
		return this.http.get<number[]>("/download/todownload/" + this.userWrapper.userId);
	}

	prepareToDownload(bookId: number, downloadType: number): Observable<number> {
		return this.http.get<number>("/download/preparetodownload/" + bookId + "/" + downloadType + "/" + this.userWrapper.userId)
	}

	downloadPreparedBook(bookId: number, downloadType: number) {
		console.info("Trying to open a new window to download bookId: " + bookId)
		window.open("/download/preparedbook/" + bookId + "/" + downloadType + "/" + this.userWrapper.userId, "_blank");
	}

}