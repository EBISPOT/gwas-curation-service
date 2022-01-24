package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.Publication;

public interface PublicationService {

    Publication getPublicationDetailsByPmidOrPubId(String pmid, Boolean isPmid);
}
