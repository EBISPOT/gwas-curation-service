package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationNotes;

import java.util.List;
import java.util.Optional;

public interface PublicationNotesRepository extends MongoRepository<PublicationNotes, String> {

    Optional<PublicationNotes> findByPublicationId(String pubId);
}
