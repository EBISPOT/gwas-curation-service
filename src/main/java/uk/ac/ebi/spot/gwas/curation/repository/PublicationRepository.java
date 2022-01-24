package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;

import java.util.Optional;
import java.util.stream.Stream;

public interface PublicationRepository extends MongoRepository<Publication, String> {

    Optional<Publication> findByPmid(String pmid);


    Optional<Publication> findById(String pubId);

}
