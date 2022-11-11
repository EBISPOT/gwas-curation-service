package uk.ac.ebi.spot.gwas.curation.solr.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import uk.ac.ebi.spot.gwas.curation.solr.domain.StudySolr;
import uk.ac.ebi.spot.gwas.deposition.solr.SOLRPublication;

import java.util.Optional;

public interface PublicationSolrRepository extends SolrCrudRepository<SOLRPublication , String> {

    @Query(value = "pmid:?0 AND curationStatus:?1 AND curator:*?2* AND title:*?3*")
    Page<SOLRPublication> findByPmidAndCurationStatusAndCuratorAndTitle(String pmid, String curationStatus, String curator,
                                                                        String title, Pageable page);

    @Query(value = "pmid:?0 AND curationStatus:?1 AND curator:*?2*")
    Page<SOLRPublication> findByPmidAndCurationStatusAndCurator(String pmid, String curationStatus, String curator,
                                                                Pageable page);

    @Query(value = "pmid:?0 AND curationStatus:?1 AND title:*?2*")
    Page<SOLRPublication> findByPmidAndCurationStatusAndTitle(String pmid, String curationStatus, String title,
                                                                Pageable page);

    @Query(value = "pmid:?0 AND curator:*?1* AND title:*?2*")
    Page<SOLRPublication> findByPmidAndCuratorAndTitle(String pmid, String curator, String title,
                                                              Pageable page);

    @Query(value = "curationStatus:?0 AND curator:*?1* AND title:*?2*")
    Page<SOLRPublication> findByCurationStatusAndCuratorAndTitle(String curationStatus, String curator, String title,
                                                       Pageable page);

    @Query(value = "pmid:?0 AND curator:*?1*")
    Page<SOLRPublication> findByPmidAndCurator(String pmid, String curator, Pageable page);

    @Query(value = "pmid:?0 AND curationStatus:?1")
    Page<SOLRPublication> findByPmidAndCurationStatus(String pmid, String curationStatus, Pageable page);

    @Query(value = "pmid:?0 AND title:*?1*")
    Page<SOLRPublication> findByPmidAndTitle(String pmid, String title, Pageable page);

    @Query(value = "curator:*?0* AND curationStatus:?1")
    Page<SOLRPublication> findByCuratorAndCurationStatus(String curator, String curationStatus, Pageable page);

    @Query(value = "curator:*?0* AND title:*?1*")
    Page<SOLRPublication> findByCuratorAndTitle(String curator, String title, Pageable page);



    @Query(value = "curationStatus:?0 AND title:*?1*")
    Page<SOLRPublication> findByCurationStatusAndTitle(String curationStatus, String title, Pageable page);

    @Query(value = "curator:*?0*")
    Page<SOLRPublication> findByCurator(String curator, Pageable page);

    @Query(value = "title:*?0*")
    Page<SOLRPublication> findByTitle(String title, Pageable page);


    @Query(value = "curationStatus:?0")
    Page<SOLRPublication> findByCurationStatus(String curationStatus, Pageable page);

    @Query(value = "pmid:?0")
    Page<SOLRPublication> findByPmid(String pmid, Pageable page);

}
