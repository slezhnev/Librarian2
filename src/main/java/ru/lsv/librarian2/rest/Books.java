package ru.lsv.librarian2.rest;

import org.jboss.resteasy.reactive.RestPath;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.Path;
import ru.lsv.librarian2.models.Book;

public class Books extends Controller {

	@CheckedTemplate
	public static class Templates {
		public static native TemplateInstance bookById(Book book); 
	}
	
	@Path("/books")
	public TemplateInstance getBookById(@RestPath Integer id) {
		return Templates.bookById(Book.findById(id));
	}
}
