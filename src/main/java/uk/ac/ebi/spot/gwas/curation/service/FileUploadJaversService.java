package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.FileUpload;
import uk.ac.ebi.spot.gwas.deposition.javers.ElementChange;

public interface FileUploadJaversService {

    public FileUpload getFileUploadDetails(String fileId);

    public String processFileUploadTag(ElementChange elementChange);
}
