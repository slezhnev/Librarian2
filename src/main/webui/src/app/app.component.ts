import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterOutlet } from '@angular/router';
import { BookInfo } from './bookinfo.component';
import { MatRadioModule } from '@angular/material/radio';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatTreeModule, MatTreeNode } from '@angular/material/tree';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { ProgressSpinner } from './progressspinner.component';
import { HttpClient } from '@angular/common/http';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { catchError } from 'rxjs';
import { Book, Author } from "./models"

interface SearchTreeNode {
  name: string;
  children?: SearchTreeNode[];
  bookId?: number;
  readed?: boolean;
  mustRead?: boolean;
  deletedInLibrary?: boolean;
}

class SearchResult {
  name: string;
  id?: string;

  constructor(name: string) {
    this.name = name;
  }

}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, BookInfo, ReactiveFormsModule, MatRadioModule, FormsModule, MatInputModule, MatFormFieldModule, MatButtonModule,
    MatListModule, MatTreeModule, MatIconModule, MatTreeNode, CommonModule, MatCheckboxModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})

export class AppComponent {
  title = 'librarian2';

  readonly http: HttpClient = inject(HttpClient)

  searchType: string = "Authors";

  searchText: string = "Дивов"

  searchTypeParam: string = "All";

  searchForm: FormGroup = new FormGroup({
    searchEdit: new FormControl()
  })

  userId: number = 3;

  searchResult: SearchResult[] = [];

  searchResultType: string = this.searchType;

  progressSpinner: ProgressSpinner = new ProgressSpinner();

  handleSearch() {
    this.searchResultType = this.searchType;
    if (this.searchResultType === 'Series') {
      let request = '';
      if (this.searchTypeParam == 'NewBooks') {
        request = '/series/newinreaded/' + this.userId + '/'
      } else
        if (this.searchTypeParam == 'Readed') {
          request = '/series/readed' + this.userId + '/'
        } else {
          request = '/series'
        }
      this.progressSpinner.openDialog();
      this.http.get<string[]>(request, {
        params: { serieName: this.searchText },
      }).pipe(
        catchError(error => {
          console.error('Cannot get series for search "' + this.searchText + '" and userId:' + this.userId);
          throw new Error('Cannot load series');
        })
      ).subscribe({
        next: data => {
          this.searchResult = data.map(el => <SearchResult>{ name: el })
          this.progressSpinner.closeDialog();
        },
        error: error => {
          this.searchResult = [<SearchResult>{ name: error.message }]
          this.progressSpinner.closeDialog();
        }
      })
    } else if (this.searchResultType === 'Authors') {
      let request = '';
      if (this.searchTypeParam == 'NewBooks') {
        request = '/authors/withNewBook/' + this.userId + '/'
      } else
        if (this.searchTypeParam == 'Readed') {
          request = '/authors/readed/' + this.userId + '/'
        } else {
          request = '/authors/byLastName'
        }
      this.progressSpinner.openDialog();
      this.http.get<Author[]>(request, {
        params: { lastName: this.searchText },
      }).pipe(
        catchError(error => {
          console.error('Cannot get authors for search "' + this.searchText + '" and userId:' + this.userId);
          throw new Error('Cannot load authors');
        })
      ).subscribe({
        next: data => {
          this.searchResult = data.map(el => <SearchResult>{ name: Author.fullName(el.firstName, el.middleName, el.lastName), id: el.authorId })
          this.progressSpinner.closeDialog();
        },
        error: error => {
          this.searchResult = [<SearchResult>{ name: error.message }]
          this.progressSpinner.closeDialog();
        }
      })
    }
  }

  searchResultSelectedElement: SearchResult | null = null;

  searchResultSelected(sr: any) {
    this.searchResultSelectedElement = sr;
    this.progressSpinner.openDialog();
    if (this.searchResultType === 'Series') {
      this.progressSpinner.openDialog();
      this.http.get<SearchTreeNode[]>('/series/books/' + this.userId + '/', {
        params: { serieName: this.searchResultSelectedElement!.name },
      }).pipe(
        catchError(error => {
          console.error('Cannot get books for serie "' + this.searchResultSelectedElement!.name + '" and userId:' + this.userId);
          throw new Error('Cannot load books for selected serie');
        })
      ).subscribe({
        next: data => {
          this.dataSource = data;
          this.progressSpinner.closeDialog();
        },
        error: error => {
          this.progressSpinner.closeDialog();
          this.dataSource = [{ name: error.message, mustRead: true, deletedInLibrary: true }]
        }
      })
    } else
      if (this.searchResultType === 'Authors') {
        this.progressSpinner.openDialog();
        this.http.get<SearchTreeNode[]>('/books/byAuthor/' + this.userId + '/', {
          params: { authorId: "" + this.searchResultSelectedElement!.id },
        }).pipe(
          catchError(error => {
            console.error('Cannot get books for author "' + this.searchResultSelectedElement!.name + '", authorId:' + this.searchResultSelectedElement!.id);
            throw new Error('Cannot load books for selected author');
          })
        ).subscribe({
          next: data => {
            this.dataSource = data;
            this.progressSpinner.closeDialog();
          },
          error: error => {
            this.progressSpinner.closeDialog();
            this.dataSource = [{ name: error.message, mustRead: true, deletedInLibrary: true }]
          }
        })
      }
  }

  processingStatus() {
    alert("Book processing status")
  }

  activeNode: any;

  childrenAccessor = (node: SearchTreeNode) => node.children ?? [];

  hasChild = (_: number, node: SearchTreeNode) => !!node.children && node.children.length > 0;

  dataSource: SearchTreeNode[] = [];

  bookInfo: Book | null = null;

  treeLeafSelected(node: SearchTreeNode) {
    if (node.bookId) {
      this.activeNode = node;
      this.progressSpinner.openDialog();
      this.http.get<Book>('/book/' + node.bookId + '/' + this.userId
      ).pipe(
        catchError(error => {
          console.error('Cannot get book for bookId: ' + node.bookId + ' and userId:' + this.userId);
          throw new Error('Cannot load selected book');
        })
      ).subscribe({
        next: data => {
          this.bookInfo = data;
          this.progressSpinner.closeDialog();
        },
        error: error => {
          this.progressSpinner.closeDialog();
          //this.dataSource = [{ name: error.message, mustRead: true, deletedInLibrary: true }]
        }
      })
    }
  }

  switchToAuthor(author: Author) {
    let sr = <SearchResult>{ name: Author.fullName(author.firstName, author.middleName, author.lastName), id: author.authorId }
    this.searchResult = [sr]
    this.searchResultSelected(sr)
  }

  switchToSerie(serieName: string) {
    let sr = <SearchResult>{ name: serieName }
    this.searchResult = [sr]
    this.searchResultSelected(sr)
  }

}
