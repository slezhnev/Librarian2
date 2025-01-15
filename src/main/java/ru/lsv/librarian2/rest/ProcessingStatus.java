package ru.lsv.librarian2.rest;

import io.quarkiverse.renarde.Controller;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import ru.lsv.librarian2.library.LibrarySheduler;
import ru.lsv.librarian2.library.LoadStatus;

public class ProcessingStatus extends Controller {

    @Inject
    LibrarySheduler sheduler;

    @Path("/processingStatus")
	@GET
	@Produces(MediaType.APPLICATION_JSON)    
    public LoadStatus getProcessingStatus() {
        sheduler.checkForNewBook();
        return LoadStatus.getInstance().clone();
    }
    
}
