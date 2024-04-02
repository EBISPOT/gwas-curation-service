package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.Curator;

import java.util.Optional;

public interface CuratorRepository extends MongoRepository<Curator, String> {

    Optional<Curator> findCuratorByLastName(String lastName);

}
