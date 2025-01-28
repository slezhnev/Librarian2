package ru.lsv.librarian2.rest;

import java.util.List;
import java.util.function.Function;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import io.quarkiverse.renarde.Controller;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Path;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.models.TreeProcessor;
import ru.lsv.librarian2.rest.Authors.AuthorView;

@Authenticated
public class Books extends Controller {

    @Inject
    JsonWebToken principal;	

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

		BookView(Book book, String userName) {
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
			this.readed = book.isReaded(userName);
			this.mustRead = book.isMustRead(userName);
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
	public BookView getBookById(@RestPath Integer id) {
		Book book = Book.findById(id);
		if (book == null) {
			return new BookView();
		} else {
			return new BookView(book, principal.getName());
		}
	}

	@Path("/books/byAuthor")
	public List<TreeProcessor.TreeNode> getBookBySerie(@RestQuery Integer authorId) {
		return TreeProcessor.convertToTree(Book.listByAuthor(authorId), principal.getName());
	}

	@Path("/books/byAuthorRaw")
	public List<BookView> getRawBookBySerie(@RestPath Integer userId, @RestQuery Integer authorId) {
		return Book.listByAuthor(authorId).stream().map(BOOK_MAPPER).collect(Collectors.toList());
	}

	@Path("/books/byTitle")
	public List<BookView> getByTitle(@RestQuery String title) {
		return Book.searchByTitle(title).stream().map(BOOK_MAPPER).collect(Collectors.toList());
	}

	private static final Logger LOG = Logger.getLogger(Books.class);
		
	@Transactional
	@Path("/book/readed")
	public RestResponse<Object> markAsReaded(@RestPath Integer bookId, @RestQuery Boolean readed) {
		Book book = Book.findById(bookId);
		if (book == null) {
			return ResponseBuilder.notFound().build();
		}
		book.readed = new HashSet<>(book.readed);
		book.readed.size();
		if (readed) {
			book.readed.add(principal.getName());
		} else {
			book.readed.remove(principal.getName());
		}
		book.persistAndFlush();
		return ResponseBuilder.ok().build();
	}

	@Transactional
	@Path("/book/mustRead")
	public RestResponse<Object> markAsMustRead(@RestPath Integer bookId, @RestQuery Boolean mustRead) {
		Book book = Book.findById(bookId);
		if (book == null) {
			return ResponseBuilder.notFound().build();
		}
		book.mustRead = new HashSet<>(book.mustRead);
		if (mustRead) {
			book.mustRead.add(principal.getName());
		} else {
			book.mustRead.remove(principal.getName());
		}
		book.persistAndFlush();
		return ResponseBuilder.ok().build();
	}

}
