package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.spot.gwas.deposition.domain.LiteratureFile;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiteratureRepository extends MongoRepository<LiteratureFile, String> {

    Optional<LiteratureFile> findByIdAndPubmedId(String id, String pubmedId);

    Page<LiteratureFile> findByPubmedId(Pageable pageable, String pubmedId);
}
