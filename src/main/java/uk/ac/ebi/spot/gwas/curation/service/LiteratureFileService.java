package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.domain.LiteratureFile;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.LiteratureFileDto;
import java.util.List;

public interface LiteratureFileService {

    List<LiteratureFile> createLiteratureFile(LiteratureFileDto fileDto, String pubmedId, User user);

    LiteratureFile getLiteratureFile(String fileId, String pubmedId);

    Page<LiteratureFile> getLiteratureFiles(Pageable pageable, String pubmedId);
}
