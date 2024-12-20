package ru.lsv.librarian2.rest;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkiverse.renarde.Controller;
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

	@Path("/author")
	public AuthorView getById(@RestPath Integer id) {
		return AUTHOR_MAPPER.apply(Author.findById(id));
	}

	@Path("/authors/byLastName")
	public List<AuthorView> getByLastName(@RestQuery String lastName) {
		return Author.search(lastName).stream().map(AUTHOR_MAPPER).collect(Collectors.toList());
	}

	@Path("/authors/withNewBook")
	public List<AuthorView> getWithNewBook(@RestPath Integer userId, @RestQuery String lastName) {
		return Author.searchWithNewBooks(userId, lastName).stream().map(AUTHOR_MAPPER).collect(Collectors.toList());
	}

	@Path("/authors/readed")
	public List<AuthorView> getReaded(@RestPath Integer userId, @RestQuery String lastName) {
		return Author.searchReaded(userId, lastName).stream().map(AUTHOR_MAPPER).collect(Collectors.toList());
	}



}
