export class Book {
	bookId: number;
	authors: Author[];
	title: string;
	genre: string;
	language: string;
	sourceLanguage: string;
	serieName: string;
	numInSerie: string;
	annotation: string;
	readed: boolean;
	mustRead: boolean;
	deletedInLibrary: boolean;
	libraryName: string;
	libraryId: number;

	constructor(bookId: number,
		authors: Author[],
		title: string,
		genre: string,
		language: string,
		sourceLanguage: string,
		serieName: string,
		numInSerie: string,
		annotation: string,
		readed: boolean,
		mustRead: boolean,
		deletedInLibrary: boolean,
		libraryName: string,
		libraryId: number) {
		this.bookId = bookId;
		this.authors = authors;
		this.title = title;
		this.genre = genre;
		this.language = language;
		this.sourceLanguage = sourceLanguage;
		this.serieName = serieName;
		this.numInSerie = numInSerie;
		this.annotation = annotation;
		this.readed = readed;
		this.mustRead = mustRead;
		this.deletedInLibrary = deletedInLibrary;
		this.libraryName = libraryName;
		this.libraryId = libraryId;
	}
}

export class Author {
	authorId: number;
	firstName: string;
	middleName: string;
	lastName: string;
	
	constructor(authorId: number, firstName: string, middleName: string, lastName: string) {
		this.authorId = authorId;
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
	}
}

