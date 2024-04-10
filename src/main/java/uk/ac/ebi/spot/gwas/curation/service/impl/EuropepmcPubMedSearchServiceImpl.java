package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.gwas.curation.config.RestInteractionConfig;
import uk.ac.ebi.spot.gwas.curation.europmc.EuropePMCTransformer;
import uk.ac.ebi.spot.gwas.curation.service.EuropepmcPubMedSearchService;
import uk.ac.ebi.spot.gwas.deposition.europmc.EuropePMCData;
import uk.ac.ebi.spot.gwas.deposition.europmc.EuropePMCRequest;
import uk.ac.ebi.spot.gwas.deposition.exception.EuropePMCException;
import uk.ac.ebi.spot.gwas.deposition.exception.PubmedLookupException;

@Service
public class EuropepmcPubMedSearchServiceImpl implements EuropepmcPubMedSearchService {

    private static final Logger log = LoggerFactory.getLogger(EuropepmcPubMedSearchServiceImpl.class);
    @Autowired
    RestInteractionConfig restInteractionConfig;

    @Autowired
    EuropePMCTransformer europePMCTransformer;

    @Autowired
    @Qualifier("restTemplateCuration")
    RestTemplate restTemplate;

    public EuropePMCData createStudyByPubmed(String pubmedId) throws PubmedLookupException,EuropePMCException {

        EuropePMCData europePMCData = null;
        //ResponseEntity<String> out = null;
        ResponseEntity<EuropePMCRequest> out = null;
        EuropePMCRequest europePMCRequest = null;
        String europepmcRoot = restInteractionConfig.getEuroPMCEndpoint();
        String europepmcSearch = restInteractionConfig.getEuroPMCSearchUrl();
        String urlRequest = null;
        if (europepmcRoot != null && europepmcSearch != null) {
            urlRequest = europepmcRoot.concat(europepmcSearch);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String queryUrl = urlRequest.replace("{idlist}", pubmedId);
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        try{
            out = restTemplate.exchange(queryUrl, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<EuropePMCRequest>() {
                    });

        } catch (EuropePMCException ex) {
            log.error("Error in RestAPI"+ex.getMessage(),ex);
            throw new EuropePMCException("Failed to connect with EuropePMC API"+pubmedId);
        }
        try {
            if(out != null){
                europePMCRequest = out.getBody();
            }
            europePMCData = europePMCTransformer.transform(europePMCRequest);

            if(europePMCData.getPublication() == null) {
                throw new PubmedLookupException("EuropePMC : Pmid not found in EuropePmc"+pubmedId);
            }


        } catch (Exception e) {
            log.error("EuropePMC : Generic Error conversion JSON"+e.getMessage(),e);
            throw new PubmedLookupException("EuropePMC : Generic Error conversion JSON"+pubmedId);
        }

        return europePMCData;

    }

}
