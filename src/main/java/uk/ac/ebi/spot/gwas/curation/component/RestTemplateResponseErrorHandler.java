package uk.ac.ebi.spot.gwas.curation.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import uk.ac.ebi.spot.gwas.deposition.exception.EuropePMCException;
import uk.ac.ebi.spot.gwas.deposition.exception.PubmedLookupException;

import java.io.IOException;

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateResponseErrorHandler.class);

    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
        return (clientHttpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR ||
                clientHttpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
        if(clientHttpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
            log.error("Inside Rest Template Client Error");
            throw new EuropePMCException("Error in retreiving details from EuropePmc API");
        }
        if(clientHttpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
            log.error("Inside Rest Template Server Error");
            throw new EuropePMCException("Error in retreiving details from EuropePmc API");
        }
    }
}
