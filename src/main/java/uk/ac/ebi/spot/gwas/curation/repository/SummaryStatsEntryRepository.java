package uk.ac.ebi.spot.gwas.curation.repository;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.SummaryStatsEntry;

import java.util.List;

@JaversSpringDataAuditable
public interface SummaryStatsEntryRepository extends MongoRepository<SummaryStatsEntry, String> {

    List<SummaryStatsEntry> findByFileUploadId(String fileId);

    List<SummaryStatsEntry> findByCallbackId(String callbackId);
}
