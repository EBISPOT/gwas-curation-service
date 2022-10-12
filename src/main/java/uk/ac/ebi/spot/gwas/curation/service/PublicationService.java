package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SearchPublicationDTO;
import uk.ac.ebi.spot.gwas.deposition.solr.SOLRPublication;

import java.util.Optional;

public interface PublicationService {

    Publication getPublicationDetailsByPmidOrPubId(String pmid, Boolean isPmid);

    Page<SOLRPublication> searchPublications(SearchPublicationDTO searchPublicationDTO, Pageable page);

    SOLRPublication getPublicationFromSolr(String id);

}
