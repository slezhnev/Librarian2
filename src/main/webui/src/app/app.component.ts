import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { BookInfo } from './bookinfo.component'

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, BookInfo],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'librarian2';
}
