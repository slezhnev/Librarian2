package ru.lsv.librarian2.rest;

import io.quarkiverse.renarde.Controller;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import ru.lsv.librarian2.library.LoadStatus;

public class ProcessingStatus extends Controller {

    @Path("/processingStatus")
	@GET
	@Produces(MediaType.APPLICATION_JSON)    
    public LoadStatus getProcessingStatus() {
        return LoadStatus.getInstance().clone();
    }
    
}
