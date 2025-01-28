import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FileSaverService } from 'ngx-filesaver';

@Injectable({
	providedIn: 'root',
})
export class DownloadService {

	readonly http: HttpClient = inject(HttpClient)

	readonly fileSaver: FileSaverService = inject(FileSaverService)

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
		console.info("Trying to use FileSaverModule to save the file")
		this.http.get("/download/preparedbook/" + bookId + "/" + downloadType + "/", {
			observe: 'response',
          	responseType: 'blob',
		  }).subscribe((res) => {
			if (res.headers.get("filename") != null) {
				console.info("Trying to save a book as " + res.headers.get("filename"))
				this.fileSaver.save(res.body, res.headers.get("filename")!, "octet/stream");
			} else {
				console.info("Cannot find header 'filename' in response")
			}
		  });
	}

}