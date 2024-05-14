package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.LiteratureFile;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.LiteratureFileDto;
import java.util.List;

public interface LiteratureFileService {

    public List<LiteratureFile> createLiteratureFile(LiteratureFileDto fileDto, String pubmedId, User user);

    public LiteratureFile getLiteratureFile(String fileId, String pubmedId);
}
