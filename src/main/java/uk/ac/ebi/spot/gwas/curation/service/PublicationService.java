package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.PublicationStatusReport;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SearchPublicationDTO;
import uk.ac.ebi.spot.gwas.deposition.solr.SOLRPublication;

import java.util.List;

public interface PublicationService {

    Publication getPublicationDetailsByPmidOrPubId(String pmid, Boolean isPmid);

    Page<SOLRPublication> searchPublications(SearchPublicationDTO searchPublicationDTO, Pageable page);

    SOLRPublication getPublicationFromSolr(String id);


    List<PublicationStatusReport>  createPublication(List<String> pmids, User user);

    PublicationDto addPublicationCurationDetails(String pmid, PublicationDto publicationDto, User user);

}
