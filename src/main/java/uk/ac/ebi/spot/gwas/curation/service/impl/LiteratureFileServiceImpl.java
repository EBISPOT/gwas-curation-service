package uk.ac.ebi.spot.gwas.curation.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.LiteratureRepository;
import uk.ac.ebi.spot.gwas.curation.service.LiteratureFileService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationService;
import uk.ac.ebi.spot.gwas.deposition.domain.LiteratureFile;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.LiteratureFileDto;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import java.util.*;

@Slf4j
@Service
public class LiteratureFileServiceImpl implements LiteratureFileService {

    @Autowired
    private FtpServiceImpl ftpService;
    @Autowired
    private PublicationService publicationService;
    @Autowired
    private LiteratureRepository literatureRepository;

    @Override
    public List<LiteratureFile> createLiteratureFile(LiteratureFileDto fileDto, String pubmedId, User user) {

        List<LiteratureFile> literatureFiles = new ArrayList<>();
        Publication publication = publicationService.getPublicationDetailsByPmidOrPubId(pubmedId, true);

        Optional.ofNullable(publication)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Pubmed id: %s not found", pubmedId)));

        fileDto.getMultipartFile().forEach(multipartFile -> {
            String newFileName = ftpService.uploadAndGetFileName(multipartFile, pubmedId);
            LiteratureFile literatureFile = LiteratureFile.builder()
                    .originalFileName(multipartFile.getOriginalFilename())
                    .onDiskFileName(newFileName)
                    .pubmedId(pubmedId)
                    .created(Provenance.builder()
                            .userId(user.getEmail())
                            .timestamp(new DateTime())
                            .build())
                    .build();
            literatureRepository.save(literatureFile);
            literatureFiles.add(literatureFile);
        });
        return literatureFiles;
    }

    @Override
    public LiteratureFile getLiteratureFile(String fileId, String pubmedId) {
        return literatureRepository.findByIdAndPubmedId(fileId, pubmedId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("File id: %s not found for pubmed id: %s", fileId, pubmedId)));
    }

    @Override
    public Map<String, Object> deleteLiteratureFile(LiteratureFile literatureFile) {
        literatureRepository.delete(literatureFile);
        return Collections.singletonMap("Literature name: " + literatureFile.getOriginalFileName(), "deleted from database");
    }

    @Override
    public Page<LiteratureFile> getLiteratureFiles(Pageable pageable, String pubmedId) {
        return literatureRepository.findByPubmedId(pageable, pubmedId);
    }

}
