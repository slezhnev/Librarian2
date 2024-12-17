package ru.lsv.librarian2.rest;

import java.util.List;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkiverse.renarde.Controller;
import jakarta.ws.rs.Path;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.models.LibUser;
import ru.lsv.librarian2.rest.Authors.AuthorView;

public class Books extends Controller {

	public static class BookView {
		public Integer bookId;
		public List<AuthorView> authors;
		public String title;
		public String genre;
		public String language;
		public String sourceLanguage;
		public String serieName;
		public Integer numInSerie;
		public String annotation;
		public Boolean readed;
		public Boolean mustRead;
		public Boolean deletedInLibrary;

		BookView(Book book, Integer userId) {
			this.bookId = book.bookId;
			this.authors = book.authors == null ? new LinkedList<>()
					: book.authors.stream().map(Authors.AUTHOR_MAPPER).collect(Collectors.toList());
			this.title = book.title;
			this.genre = book.genre;
			this.language = book.language;
			this.sourceLanguage = book.sourceLanguage;
			this.serieName = book.serieName;
			this.numInSerie = book.numInSerie;
			this.annotation = book.annotation;
			this.readed = book.isReaded(userId);
			this.mustRead = book.isMustRead(userId);
			this.deletedInLibrary = book.deletedInLibrary;
		}

		BookView() {
			this.bookId = null;
			this.authors = new LinkedList<>();
			this.title = null;
			this.genre = null;
			this.language = null;
			this.sourceLanguage = null;
			this.serieName = null;
			this.numInSerie = null;
			this.annotation = null;
			this.readed = null;
			this.mustRead = null;
			this.deletedInLibrary = null;
		}

	}

	@Path("/book")
	public BookView getBookById(@RestPath Integer id, @RestPath Integer userId) {
		Book book = Book.findById(id);
		if (book == null) {
			return new BookView();
		} else {
			return new BookView(book, userId);
		}
	}

	// @Path("/books/bySerie")
	// public TemplateInstance getBookBySerie(@RestQuery String serieName) {
	// return Templates.books(Book.listBySerie(serieName));
	// }

}
