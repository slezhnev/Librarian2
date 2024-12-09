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
    this.searchResult = [this.searchForm.value.searchEdit + "-1", this.searchForm.value.searchEdit + "-2"]
  }

  searchResultType: string = this.searchType;

  searchResultFormControl = new FormControl();
  searchResultForm: FormGroup = new FormGroup({
    searchResultSelected: this.searchResultFormControl,
  })

  searchLIselected(event: MatSelectionListChange) {
    alert("was selected " + this.searchResultFormControl.value)
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
  ];


}
