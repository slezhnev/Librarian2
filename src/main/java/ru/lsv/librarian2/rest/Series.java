package ru.lsv.librarian2.rest;

import java.util.List;

import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import io.quarkiverse.renarde.Controller;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.Path;
import ru.lsv.librarian2.models.Book;

public class Series extends Controller {

	@CheckedTemplate
	public static class Templates {
		public static native TemplateInstance series(List<String> series);
	}

	@Path("/series")
	public TemplateInstance getSeries(@RestQuery String serieName) {
		return Templates.series(Book.searchBySerie(serieName));
	}

	@Path("/readedseries")
	public TemplateInstance getReadedSeries(@RestPath Integer userId, @RestQuery String serieName) {
		return Templates.series(Book.searchForReadedSeries(serieName, userId));
	}

	@Path("/newinreadedseries")
	public TemplateInstance getSeriesWithNewBooks(@RestPath Integer userId, @RestQuery String serieName) {
		return Templates.series(Book.searchSeriesWithNewBooks(serieName, userId));
	}

}
