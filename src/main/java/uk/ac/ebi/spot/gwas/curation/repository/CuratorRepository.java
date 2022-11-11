package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.Curator;

public interface CuratorRepository extends MongoRepository<Curator, String> {


}
