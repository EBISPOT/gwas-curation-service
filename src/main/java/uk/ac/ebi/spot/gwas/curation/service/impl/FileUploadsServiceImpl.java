package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.FileUploadRepository;
import uk.ac.ebi.spot.gwas.curation.service.FileUploadsService;
import uk.ac.ebi.spot.gwas.deposition.domain.FileUpload;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import java.util.Optional;

@Service
public class FileUploadsServiceImpl implements FileUploadsService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadsService.class);

    @Autowired
    private FileUploadRepository fileUploadRepository;

    @Override
    public FileUpload getFileUpload(String fileUploadId) {
        log.info("Retrieving file upload: {}", fileUploadId);
        Optional<FileUpload> optionalFileUpload = fileUploadRepository.findById(fileUploadId);
        if (!optionalFileUpload.isPresent()) {
            log.error("Unable to find file upload: {}", fileUploadId);
            throw new EntityNotFoundException("Unable to find file upload: " + fileUploadId);
        }
        log.info("File upload successfully retrieved: {}", optionalFileUpload.get().getId());
        return optionalFileUpload.get();
    }

}
