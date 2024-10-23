package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuthorsSort;

import java.util.List;
import java.util.Optional;

public interface PublicationAuthorsSortRepository extends MongoRepository<PublicationAuthorsSort, String> {

     Optional<PublicationAuthorsSort> findByPublicationIdAndAuthorIdAndSort(String publicationId, String authorId, Integer sort);

     List<PublicationAuthorsSort> findByPublicationId(String publicationId);

     Optional<PublicationAuthorsSort>  findByPublicationIdAndAuthorId(String publicationId, String authorId);
}
