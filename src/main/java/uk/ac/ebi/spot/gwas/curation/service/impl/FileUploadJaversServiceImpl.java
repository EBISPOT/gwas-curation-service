package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.service.FileUploadJaversService;
import uk.ac.ebi.spot.gwas.curation.service.FileUploadsService;
import uk.ac.ebi.spot.gwas.deposition.domain.FileUpload;
import uk.ac.ebi.spot.gwas.deposition.javers.ElementChange;

@Service
public class FileUploadJaversServiceImpl implements FileUploadJaversService {

        @Autowired
    FileUploadsService fileUploadsService;

    public FileUpload getFileUploadDetails(String fileId){
        return fileUploadsService.getFileUpload(fileId);
    }

    public String processFileUploadTag(ElementChange elementChange) {
        if (elementChange.getElementChangeType().equals("ValueAdded")){
            return elementChange.getValue().toString();
        }
        else if(elementChange.getElementChangeType().equals("ValueRemoved")){
            return elementChange.getValue().toString();
        }
        return null;
    }

}
