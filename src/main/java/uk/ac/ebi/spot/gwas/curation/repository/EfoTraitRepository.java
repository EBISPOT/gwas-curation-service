package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;

public interface EfoTraitRepository extends MongoRepository<EfoTrait, String> {

    Page<EfoTrait> findByTrait(String trait, Pageable page);
}
