package uk.ac.ebi.spot.gwas.curation.repository;

import org.bson.BSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import java.util.Optional;

public interface PublicationRepository extends MongoRepository<Publication, String> {

    Optional<Publication> findByPmid(String pmid);


    Optional<Publication> findById(String pubId);

    @Query(value = "?0")
    Page<Publication> findByQuery(BSONObject query, Pageable pageable);

}
