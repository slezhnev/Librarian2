import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoadStatus } from './models';

@Injectable({
  providedIn: 'root'
})
export class BooksProcessingStatusService {
  private apiUrl = '/processingStatus'; 

  constructor(private http: HttpClient) {}

  getStatus(): Observable<LoadStatus> {
    return this.http.get(this.apiUrl);
  }
}