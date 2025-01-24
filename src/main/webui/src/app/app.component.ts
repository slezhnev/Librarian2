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
import { Book, Author } from "./models/models"
import { MatGridListModule } from '@angular/material/grid-list';
import { BooksUpdateStatusDialog } from './booksstatus.component'
import { BooksDownloadDialog } from './booksdownload.component'
import Keycloak from 'keycloak-js';

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
    MatListModule, MatTreeModule, MatIconModule, MatTreeNode, CommonModule, MatCheckboxModule, MatGridListModule],
  templateUrl: './app.component.grid.html',
  styleUrl: './app.component.grid.css',
})

export class AppComponent {

  title = 'librarian2';

  readonly http: HttpClient = inject(HttpClient)

  private readonly keycloak = inject(Keycloak);

  logout() {
    this.keycloak.logout();
  }

  searchType: string = "Books";

  searchText: string = "Боги"

  searchTypeParam: string = "All";

  searchForm: FormGroup = new FormGroup({
    searchEdit: new FormControl()
  })

  searchResult: SearchResult[] = [];

  searchResultType: string = this.searchType;

  readonly progressSpinner: ProgressSpinner = new ProgressSpinner();

  readonly statusDialog: BooksUpdateStatusDialog = new BooksUpdateStatusDialog();
  readonly downloadDialog: BooksDownloadDialog = new BooksDownloadDialog();

  isSearchTypeBooks(): boolean {
    if (this.searchType === 'Books') {
      this.searchTypeParam = 'All'
      return true;
    } else {
      return false;
    }
  }

  handleSearch() {
    this.searchResultType = this.searchType;
    if (this.searchResultType === 'Series') {
      let request = '';
      if (this.searchTypeParam == 'NewBooks') {
        request = '/series/newinreaded/'
      } else
        if (this.searchTypeParam == 'Readed') {
          request = '/series/readed/'
        } else {
          request = '/series'
        }
      this.progressSpinner.openDialog();
      this.http.get<string[]>(request, {
        params: { serieName: this.searchText },
      }).pipe(
        catchError(error => {
          console.error('Cannot get series for search "' + this.searchText + '"');
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
        request = '/authors/withNewBook/'
      } else
        if (this.searchTypeParam == 'Readed') {
          request = '/authors/readed/'
        } else {
          request = '/authors/byLastName'
        }
      this.progressSpinner.openDialog();
      this.http.get<Author[]>(request, {
        params: { lastName: this.searchText },
      }).pipe(
        catchError(error => {
          console.error('Cannot get authors for search "' + this.searchText + '"');
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
    } else if (this.searchResultType === 'Books') {
      this.progressSpinner.openDialog();
      this.http.get<Book[]>('/books/byTitle', {
        params: { title: this.searchText },
      }).pipe(
        catchError(error => {
          console.error('Cannot get books for search "' + this.searchText + '"');
          throw new Error('Cannot load authors');
        })
      ).subscribe({
        next: data => {
          this.searchResult = data.map(el => <SearchResult>{ name: el.title, id: el.bookId })
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
      this.http.get<SearchTreeNode[]>('/series/books/', {
        params: { serieName: this.searchResultSelectedElement!.name },
      }).pipe(
        catchError(error => {
          console.error('Cannot get books for serie "' + this.searchResultSelectedElement!.name);
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
        this.http.get<SearchTreeNode[]>('/books/byAuthor/', {
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
      } else
        if (this.searchResultType === 'Books') {
          this.progressSpinner.openDialog();
          this.http.get<Book>('/book/' + this.searchResultSelectedElement!.id
          ).pipe(
            catchError(error => {
              console.error('Cannot get book for bookId "' + this.searchResultSelectedElement!.id);
              throw new Error('Cannot load book');
            })
          ).subscribe({
            next: data => {
              const sr = <SearchTreeNode>{
                name: data.title!,
                children: [],
                bookId: data.bookId!,
                readed: data.readed,
                mustRead: data.mustRead,
                deletedInLibrary: data.deletedInLibrary
              };
              this.dataSource = [sr];
              this.progressSpinner.closeDialog();
              this.treeLeafSelected(sr)
            },
            error: error => {
              this.progressSpinner.closeDialog();
              this.dataSource = [{ name: error.message, mustRead: true, deletedInLibrary: true }]
            }
          })
        }
  }

  processingStatus() {
    this.statusDialog.openDialog();
  }

  downloadBooks() {
    this.downloadDialog.openDialog();    
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
      this.http.get<Book>('/book/' + node.bookId
      ).pipe(
        catchError(error => {
          console.error('Cannot get book for bookId: ' + node.bookId);
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
    this.searchResultType = 'Authors'
    this.searchResult = [sr]
    this.searchResultSelected(sr)
  }

  switchToSerie(serieName: string) {
    let sr = <SearchResult>{ name: serieName }
    this.searchResultType = 'Series'
    this.searchResult = [sr]
    this.searchResultSelected(sr)
  }

  selectForDownload(node: SearchTreeNode) {
    let temp = true
    if (node.mustRead) {
      temp = false;
    }
    this.http.get('/book/mustRead/' + node.bookId + '/', {
      params: { mustRead: temp },
    }).pipe(
      catchError(error => {
        console.error('Cannot set MustRead mark for bookId: ' + node.bookId);
        throw new Error('Cannot load MustRead mark for book');
      })
    ).subscribe({
      next: data => {
        node.mustRead = temp
      },
      error: error => {
      }
    })
  }

  selectAsReaded(node: SearchTreeNode) {
    let temp = true
    if (node.readed) {
      temp = false;
    }
    this.http.get('/book/readed/' + node.bookId + '/', {
      params: { readed: temp },
    }).pipe(
      catchError(error => {
        console.error('Cannot set Readed mark for bookId: ' + node.bookId);
        throw new Error('Cannot load Readed mark for book');
      })
    ).subscribe({
      next: data => {
        node.readed = temp
      },
      error: error => {
      }
    })
  }

}
