package uk.ac.ebi.spot.gwas.curation.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mashape.unirest.http.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.spot.gwas.curation.config.RestInteractionConfig;
import uk.ac.ebi.spot.gwas.curation.europmc.EuropePMCDeserializer;
import uk.ac.ebi.spot.gwas.curation.service.EuropepmcPubMedSearchService;
import uk.ac.ebi.spot.gwas.deposition.europmc.EuropePMCData;
import uk.ac.ebi.spot.gwas.deposition.exception.EuropePMCException;
import uk.ac.ebi.spot.gwas.deposition.exception.PubmedLookupException;

import java.io.IOException;

@Service
public class EuropepmcPubMedSearchServiceImpl implements EuropepmcPubMedSearchService {

    private static final Logger log = LoggerFactory.getLogger(EuropepmcPubMedSearchServiceImpl.class);
    @Autowired
    RestInteractionConfig restInteractionConfig;


    @Autowired
    @Qualifier("restTemplateCuration")
    RestTemplate restTemplate;

    public EuropePMCData createStudyByPubmed(String pubmedId) throws PubmedLookupException,EuropePMCException {

        EuropePMCData europePMCData = null;
        ResponseEntity<String> out = null;
        String europepmcRoot = restInteractionConfig.getEuroPMCEndpoint();
        String europepmcSearch = restInteractionConfig.getEuroPMCSearchUrl();
        String urlRequest = null;
        JsonNode body = null;
        if (europepmcRoot != null && europepmcSearch != null) {
            urlRequest = europepmcRoot.concat(europepmcSearch);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String queryUrl = urlRequest.replace("{idlist}", pubmedId);
        HttpEntity<Object> entity = new HttpEntity<Object>(headers);
        try{
            out = restTemplate.exchange(queryUrl, HttpMethod.GET, entity, String.class);
            body = new JsonNode(out.getBody().toString());

        } catch (EuropePMCException ex) {
            log.error("Error in RestAPI"+ex.getMessage(),ex);
            throw new EuropePMCException("Failed to connect with EuropePMC API"+pubmedId);
        }

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(EuropePMCData.class, new EuropePMCDeserializer());
        mapper.registerModule(module);

        try {
            europePMCData = mapper.readValue(body.toString(), EuropePMCData.class);

            if(europePMCData.getPublication() == null) {
                throw new PubmedLookupException("EuropePMC : Pmid not found in EuropePmc"+pubmedId);
            }

        } catch (IOException ioe) {
           log.error("EuropePMC : IO Exception - JSON conversion"+ioe.getMessage(),ioe);
            throw new PubmedLookupException("EuropePMC : Pmid not found in EuropePmc"+pubmedId);

        }catch (Exception e) {
            log.error("EuropePMC : Generic Error conversion JSON"+e.getMessage(),e);
            throw new PubmedLookupException("EuropePMC : Generic Error conversion JSON"+pubmedId);
        }

        return europePMCData;

    }

}
