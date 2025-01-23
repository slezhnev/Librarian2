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

	static fullName(firstName: string | undefined, middleName: string | undefined, lastName: string | undefined): string {
		let res: string = '';
		if (lastName) {
			res = lastName + " ";
		}
		if (firstName) {
			res = res + firstName + " "
		}
		if (middleName) {
			res = res + middleName
		}
		return res
	}

}

export class LoadStatus {
	currentLibrary?: string
	totalArcsToProcess?: number;
	currentArcsToProcess?: number;
	currentArcName? : string;
	totalFilesToProcess?: number;
	currentFileToProcess?: number;
	wasErrorOnLoad?: boolean;
	checkinginProgress?: boolean;
}

