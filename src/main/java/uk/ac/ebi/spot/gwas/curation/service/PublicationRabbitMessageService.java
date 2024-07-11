package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuthor;

import java.util.List;

public interface PublicationRabbitMessageService {

    List<PublicationAuthor> getAuthorDetails(List<String> authorIds);

    PublicationAuthor getFirstAuthor(String authorId);

}
