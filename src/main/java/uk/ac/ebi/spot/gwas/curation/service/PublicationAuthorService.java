package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuthor;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.PublicationAuthorDto;
import uk.ac.ebi.spot.gwas.deposition.europmc.EuropePMCData;

import java.util.List;
import java.util.Optional;

public interface PublicationAuthorService {

    public List<String> addAuthorsForPublication(EuropePMCData europePMCData, User user, String publicationId);

    public String getFirstAuthorDetails(PublicationAuthorDto publicationAuthorDto, User user);

    public Optional<PublicationAuthor> getAuthorDetail(String seqId);
}
