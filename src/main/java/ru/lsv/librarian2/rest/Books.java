package ru.lsv.librarian2.rest;

import java.util.List;
import java.util.function.Function;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import io.quarkiverse.renarde.Controller;
import jakarta.ws.rs.Path;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.models.LibUser;
import ru.lsv.librarian2.models.TreeProcessor;
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

		BookView(Book book) {
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
			this.readed = null;
			this.mustRead = null;
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

	public static Function<Book, BookView> BOOK_MAPPER = el -> new BookView(el);

	@Path("/book")
	public BookView getBookById(@RestPath Integer id, @RestPath Integer userId) {
		Book book = Book.findById(id);
		if (book == null) {
			return new BookView();
		} else {
			return new BookView(book, userId);
		}
	}

	@Path("/books/byAuthor")
	public List<TreeProcessor.TreeNode> getBookBySerie(@RestPath Integer userId, @RestQuery Integer authorId) {
		return TreeProcessor.convertToTree(Book.listByAuthor(authorId), userId);
	}

	@Path("/books/byAuthorRaw")
	public List<BookView> getRawBookBySerie(@RestPath Integer userId, @RestQuery Integer authorId) {
		return Book.listByAuthor(authorId).stream().map(BOOK_MAPPER).collect(Collectors.toList());
	}

	@Path("/books/byTitle")
	public List<BookView> getByTitle(@RestQuery String title) {
		return Book.searchByTitle(title).stream().map(BOOK_MAPPER).collect(Collectors.toList());
	}

	@Path("/book/readed")
	public RestResponse<Object> markAsReaded(@RestPath Integer userId, @RestQuery Integer bookId, @RestQuery Boolean readed) {
		Book book = Book.findById(bookId);
		if (book == null) {
			return ResponseBuilder.notFound().build();
		}
		LibUser user = LibUser.findById(userId);
		if (user == null) {
			return ResponseBuilder.notFound().build();
		}
		if (!readed) {
			book.readed.add(user);
		} else {
			book.readed.remove(user);
		}
		book.persist();
		return ResponseBuilder.ok().build();
	}

	@Path("/book/mustRead")
	public RestResponse<Object> markAsMustRead(@RestPath Integer userId, @RestQuery Integer bookId, @RestQuery Boolean mustRead) {
		Book book = Book.findById(bookId);
		if (book == null) {
			return ResponseBuilder.notFound().build();
		}
		LibUser user = LibUser.findById(userId);
		if (user == null) {
			return ResponseBuilder.notFound().build();
		}
		if (!mustRead) {
			book.mustRead.add(user);
		} else {
			book.mustRead.remove(user);
		}
		book.persist();
		return ResponseBuilder.ok().build();
	}

}
