import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { RouterOutlet } from '@angular/router';
import { BookInfo } from './bookinfo.component';
import { MatRadioModule } from '@angular/material/radio';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule, MatSelectionListChange } from '@angular/material/list';
import { MatTreeModule, MatTreeNode } from '@angular/material/tree';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { ProgressSpinner } from './progressspinner.component';

interface SearchTreeNode {
  name: string;
  children?: SearchTreeNode[];
}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, BookInfo, ReactiveFormsModule, MatRadioModule, FormsModule, MatInputModule, MatFormFieldModule, MatButtonModule, 
    MatListModule, MatTreeModule, MatIconModule, MatTreeNode, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})

export class AppComponent {
  title = 'librarian2';

  searchType: string = "Authors";

  searchForm: FormGroup = new FormGroup({
    searchEdit: new FormControl('', Validators.required)
  })

  searchResult: string[] = [];

  handleSearch() {
    this.searchResultType = this.searchType;
    this.searchResult = [];
    for (var i = 0; i < 50; i++) {
      this.searchResult[i] = this.searchForm.value.searchEdit + i;
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

  activeNode : any;

  childrenAccessor = (node: SearchTreeNode) => node.children ?? [];

  hasChild = (_: number, node: SearchTreeNode) => !!node.children && node.children.length > 0;

  dataSource : SearchTreeNode[] = [
    {
      name: 'Fruit',
      children: [{name: 'Apple'}, {name: 'Banana'}, {name: 'Fruit loops'}],
    },
    {
      name: 'Vegetables',
      children: [
        {
          name: 'Green',
          children: [{name: 'Broccoli'}, {name: 'Brussels sprouts'}],
        },
        {
          name: 'Orange',
          children: [{name: 'Pumpkins'}, {name: 'Carrots'}],
        },
      ],
    },
    { name: 'Book itself' },
  ];


}
