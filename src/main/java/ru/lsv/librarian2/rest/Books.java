package ru.lsv.librarian2.rest;

import java.util.List;

import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.Path;
import ru.lsv.librarian2.models.Book;

public class Books extends Controller {

	// @CheckedTemplate
	// public static class Templates {
	// 	public static native TemplateInstance bookById(Book book);
	// 	public static native TemplateInstance books(List<Book> books);
	// }
	
	// @Path("/books")
	// public TemplateInstance getBookById(@RestPath Integer id) {
	// 	return Templates.bookById(Book.findById(id));
	// }
	
	// @Path("/books/bySerie")
	// public TemplateInstance getBookBySerie(@RestQuery String serieName) {
	// 	return Templates.books(Book.listBySerie(serieName));
	// }
	
}
