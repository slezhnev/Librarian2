export class Book {
	bookId?: number | null;
	authors?: Author[];
	title?: string;
	genre?: string;
	language?: string;
	sourceLanguage?: string;
	serieName?: string;
	numInSerie?: string;
	annotation?: string;
	readed?: boolean;
	mustRead?: boolean;
	deletedInLibrary?: boolean;
	libraryId?: number;
}

export class Author {
	authorId?: number | null;
	firstName?: string;
	middleName?: string;
	lastName?: string;
}

