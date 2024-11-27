package ru.lsv.librarian2.rest;

import java.util.List;

import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.Path;
import ru.lsv.librarian2.models.Author;

public class Authors extends Controller {

	@CheckedTemplate
	public static class Templates {
		public static native TemplateInstance authorById(Author author);

		public static native TemplateInstance authors(List<Author> authors);
	}

	@Path("/authors")
	public TemplateInstance getById(@RestPath Integer id) {
		return Templates.authorById(Author.findById(id));
	}

	@Path("/authors/byName")
	public TemplateInstance getByLastName(@RestQuery String lastName) {
		return Templates.authors(Author.search(lastName));
	}
}
