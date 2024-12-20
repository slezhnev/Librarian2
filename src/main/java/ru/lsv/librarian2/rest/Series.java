package ru.lsv.librarian2.rest;

import java.util.List;

import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkiverse.renarde.Controller;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.models.TreeProcessor;

public class Series extends Controller {

	@Path("/series")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getSeries(@RestQuery String serieName) {
		return Book.searchBySerie(serieName);
	}

	@Path("/series/readed")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getReadedSeries(@RestPath Integer userId, @RestQuery String serieName) {
		return Book.searchForReadedSeries(serieName, userId);
	}

	@Path("/series/newinreaded")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getSeriesWithNewBooks(@RestPath Integer userId, @RestQuery String serieName) {
		return Book.searchSeriesWithNewBooks(serieName, userId);
	}

	@Path("/series/books")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TreeProcessor.TreeNode> getTreeBySerie(@RestPath Integer userId, @RestQuery String serieName) {
		return TreeProcessor.convertToTree(Book.listBySerie(serieName), userId);
	}

}
