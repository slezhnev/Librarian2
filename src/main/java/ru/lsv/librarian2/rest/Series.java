package ru.lsv.librarian2.rest;

import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkiverse.renarde.Controller;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.models.TreeProcessor;

@Authenticated
public class Series extends Controller {

    @Inject
    JsonWebToken principal;	

	@Path("/series")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getSeries(@RestQuery String serieName) {
		return Book.searchBySerie(serieName);
	}

	@Path("/series/readed")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getReadedSeries(@RestQuery String serieName) {
		return Book.searchForReadedSeries(serieName, principal.getName());
	}

	@Path("/series/newinreaded")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getSeriesWithNewBooks(@RestQuery String serieName) {
		return Book.searchSeriesWithNewBooks(serieName, principal.getName());
	}

	@Path("/series/books")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TreeProcessor.TreeNode> getTreeBySerie(@RestQuery String serieName) {
		return TreeProcessor.convertToTree(Book.listBySerie(serieName), principal.getName());
	}

}
