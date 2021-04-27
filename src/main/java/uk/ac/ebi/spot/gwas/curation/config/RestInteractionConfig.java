package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RestInteractionConfig {

    @Value("${gwas-deposition-service.fileupload.url}")
    private String depositionServiceUrl;

    @Value("${gwas-deposition-service.fileupload.endpoints.submission}")
    private String submissionEndpoint;

    public String getDepositionServiceUrl() {
        return depositionServiceUrl;
    }

    public String getSubmissionEndpoint() { return submissionEndpoint; }
}
