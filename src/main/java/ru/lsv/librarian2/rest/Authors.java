package ru.lsv.librarian2.rest;

import java.util.List;
import java.util.function.Function;

import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.Path;
import ru.lsv.librarian2.models.Author;

public class Authors extends Controller {

	public static class AuthorView {
		public Integer authorId;
		public String firstName;
		public String middleName;
		public String lastName;

		public AuthorView(Integer authorId, String firstName, String lastName, String middleName) {
			this.authorId = authorId;
			this.firstName = firstName;
			this.lastName = lastName;
			this.middleName = middleName;
		}
	}

	public static Function<Author, AuthorView> AUTHOR_MAPPER = a -> new AuthorView(a.authorId, a.firstName, a.lastName,
			a.middleName);

	// @CheckedTemplate
	// public static class Templates {
	// public static native TemplateInstance authorById(Author author);

	// public static native TemplateInstance authors(List<Author> authors);
	// }

	// @Path("/authors")
	// public TemplateInstance getById(@RestPath Integer id) {
	// return Templates.authorById(Author.findById(id));
	// }

	// @Path("/authors/byName")
	// public TemplateInstance getByLastName(@RestPath Integer libraryId, @RestQuery
	// String lastName) {
	// return Templates.authors(Author.search(lastName, libraryId));
	// }

	// @Path("/authors/withNewBooks")
	// public TemplateInstance searchWithNewBooks(@RestPath Integer userId,
	// @RestQuery String lastName) {
	// return Templates.authors(Author.searchWithNewBooks(userId, lastName));
	// }

}
