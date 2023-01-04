package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.europmc.EuropePMCData;
import uk.ac.ebi.spot.gwas.deposition.exception.EuropePMCException;
import uk.ac.ebi.spot.gwas.deposition.exception.PubmedLookupException;

public interface EuropepmcPubMedSearchService {

    public EuropePMCData createStudyByPubmed(String pubmedId) throws PubmedLookupException, EuropePMCException;

}
