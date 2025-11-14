package uk.ac.ebi.spot.gwas.curation.oracle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.ac.ebi.spot.gwas.curation.oracle.model.DiseaseTrait;
import java.util.Optional;

public interface DiseaseTraitsRepository extends JpaRepository<DiseaseTrait, Long> {

    Optional<DiseaseTrait> findByTrait(String trait);
}
