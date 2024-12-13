import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
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
import { AsyncPipe } from '@angular/common';
import { Observable } from 'rxjs';

interface SearchTreeNode {
  name: string;
  children?: SearchTreeNode[];
  bookId?: number;
}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, BookInfo, ReactiveFormsModule, MatRadioModule, FormsModule, MatInputModule, MatFormFieldModule, MatButtonModule,
    MatListModule, MatTreeModule, MatIconModule, MatTreeNode, CommonModule, MatCheckboxModule, AsyncPipe],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})

export class AppComponent {
  title = 'librarian2';

  readonly http: HttpClient = inject(HttpClient)

  searchType: string = "Series";

  searchText: string = "S-T-I-K-S"

  searchTypeParam: string = "All";

  searchForm: FormGroup = new FormGroup({
    searchEdit: new FormControl()
  })

  userId: number = 3;

  searchResult$: Observable<string[]> | null = null;

  handleSearch() {
    this.searchResultType = this.searchType;
    if (this.searchResultType === 'Series') {
      let request = '';
      if (this.searchTypeParam == 'NewBooks') {
        request = '/newinreadedseries/' + this.userId + '/'
      } else
        if (this.searchTypeParam == 'Readed') {
          request = '/readedseries/' + this.userId + '/'
        } else {
          request = '/series'
        }
      this.progressSpinner.openDialog();
      this.searchResult$ = this.http.get<string[]>(request, {
        params: { serieName: this.searchText },
      })
      this.searchResult$.subscribe(() => {
        this.progressSpinner.closeDialog();
      })
    }
  }

  searchResultType: string = this.searchType;

  progressSpinner: ProgressSpinner = new ProgressSpinner();

  searchResultSelectedElement: any;

  searchResultSelected(sr: any) {
    this.progressSpinner.openDialog();
    this.searchResultSelectedElement = sr;
    setTimeout(() => {
      this.progressSpinner.closeDialog();
    }, 5000);
  }

  processingStatus() {
    alert("Book processing status")
  }

  activeNode: any;

  childrenAccessor = (node: SearchTreeNode) => node.children ?? [];

  hasChild = (_: number, node: SearchTreeNode) => !!node.children && node.children.length > 0;

  dataSource: SearchTreeNode[] = [
    {
      name: 'Fruit',
      children: [{ name: 'Apple' }, { name: 'Banana' }, { name: 'Fruit loops' }],
    },
    {
      name: 'Vegetables',
      children: [
        {
          name: 'Green',
          children: [{ name: 'Broccoli' }, { name: 'Brussels sprouts' }],
        },
        {
          name: 'Orange',
          children: [{ name: 'Pumpkins' }, { name: 'Carrots' }],
        },
      ],
    },
    { name: 'Book itself' },
  ];

  treeLeafSelected(node: any) {
    this.activeNode = node;
  }

}
