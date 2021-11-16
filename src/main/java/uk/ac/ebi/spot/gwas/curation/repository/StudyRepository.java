package uk.ac.ebi.spot.gwas.curation.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;

import java.util.List;
import java.util.stream.Stream;


public interface StudyRepository extends MongoRepository<Study, String> {

    Stream<Study> readByIdIn(List<String> ids);

    Stream<Study> findByEfoTraitListContains(String traitId);
}
