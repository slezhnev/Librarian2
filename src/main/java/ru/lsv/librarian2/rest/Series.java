package ru.lsv.librarian2.rest;

import java.util.List;

import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkiverse.renarde.Controller;
import jakarta.ws.rs.Path;
import ru.lsv.librarian2.models.Book;

public class Series extends Controller {

	@Path("/series")
	public List<String> getSeries(@RestQuery String serieName) {
		return Book.searchBySerie(serieName);
	}

	@Path("/readedseries")
	public List<String> getReadedSeries(@RestPath Integer userId, @RestQuery String serieName) {
		return Book.searchForReadedSeries(serieName, userId);
	}

	@Path("/newinreadedseries")
	public List<String> getSeriesWithNewBooks(@RestPath Integer userId, @RestQuery String serieName) {
		return Book.searchSeriesWithNewBooks(serieName, userId);
	}

}
