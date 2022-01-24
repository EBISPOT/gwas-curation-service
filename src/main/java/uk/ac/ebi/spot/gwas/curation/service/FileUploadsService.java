package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.deposition.domain.FileUpload;

import java.io.InputStream;
import java.util.List;

public interface FileUploadsService {

    public List<FileUpload> getFileUploads(List<String> ids);

    FileUpload getFileUpload(String fileUploadId);


}
