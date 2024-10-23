package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuthor;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuthorsSort;

import java.util.List;
import java.util.Map;

public interface PublicationRabbitMessageService {

    Map<Integer, PublicationAuthor> getAuthorDetails(List<String> authorIds, String publicationId);

    PublicationAuthor getFirstAuthor(String authorId);


}
