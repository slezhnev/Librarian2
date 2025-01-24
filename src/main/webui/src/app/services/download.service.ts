import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
	providedIn: 'root',
})
export class DownloadService {

	readonly http: HttpClient = inject(HttpClient)

	downloadBook(bookId: number, downloadType: number) {
		window.open("/download/book/" + bookId + "/" + downloadType + "/", "_blank");
	}

	getBooksToDownload(): Observable<number[]> {
		return this.http.get<number[]>("/download/todownload/");
	}

	prepareToDownload(bookId: number, downloadType: number): Observable<number> {
		return this.http.get<number>("/download/preparetodownload/" + bookId + "/" + downloadType + "/")
	}

	downloadPreparedBook(bookId: number, downloadType: number) {
		console.info("Trying to open a new window to download bookId: " + bookId)
		window.open("/download/preparedbook/" + bookId + "/" + downloadType + "/", "_blank");
	}

}