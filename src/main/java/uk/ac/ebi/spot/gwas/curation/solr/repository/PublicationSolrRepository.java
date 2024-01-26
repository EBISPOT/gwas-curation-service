package uk.ac.ebi.spot.gwas.curation.solr.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import uk.ac.ebi.spot.gwas.deposition.solr.SOLRPublication;

public interface PublicationSolrRepository extends SolrCrudRepository<SOLRPublication , String> {

    @Query(value = "?0 AND  ?1 AND ?2 AND ?3 AND ?4")
    Page<SOLRPublication> findPublications(String pmid, String title, String curator, String curationStatus, String submitter, Pageable page);

}
