package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationNotes;

import java.util.List;
import java.util.Optional;

public interface PublicationNotesRepository extends MongoRepository<PublicationNotes, String> {

    Page<PublicationNotes> findByPublicationId(String pubId, Pageable pageable);

    Optional<PublicationNotes> findById(String noteId);
    Boolean existsByPublicationId(String pubId);
}
