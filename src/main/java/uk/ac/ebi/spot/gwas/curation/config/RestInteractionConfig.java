package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RestInteractionConfig {

    @Value("${gwas-deposition-service.fileupload.endpoint}")
    private String depositionServiceUrl;

    public String getDepositionServiceUrl() {
        return depositionServiceUrl;
    }
}
