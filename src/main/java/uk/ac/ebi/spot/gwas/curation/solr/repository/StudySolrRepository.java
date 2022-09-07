package uk.ac.ebi.spot.gwas.curation.solr.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.SolrCrudRepository;
import uk.ac.ebi.spot.gwas.curation.solr.domain.StudySolr;

public interface StudySolrRepository extends SolrCrudRepository<StudySolr, String> {

    public Page<StudySolr> findByEfoTraitsContaining(String efoTrait, Pageable page);

    public Page<StudySolr> findByReportedTraitContaining(String reportedTrait, Pageable page);

    public Page<StudySolr> findByNotesContainingOrEfoTraitsContainingOrReportedTraitContaining(String reportedTrait, Pageable page);


}

