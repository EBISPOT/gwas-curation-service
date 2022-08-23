package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface EfoTraitRepository extends MongoRepository<EfoTrait, String> {

    Page<EfoTrait> findByTraitContainingIgnoreCase(String trait, Pageable page);
    List<EfoTrait> findByTraitContainingIgnoreCase(String trait);
    Optional<EfoTrait> findByShortForm(String shortForm);
    Stream<EfoTrait> findByShortFormIn(Set<String> shortForms);
}
