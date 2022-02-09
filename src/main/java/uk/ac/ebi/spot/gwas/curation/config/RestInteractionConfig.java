package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RestInteractionConfig {

    @Value("${gwas-deposition-service.url}")
    private String depositionServiceUrl;

    @Value("${gwas-curation-service.url}")
    private String curationServiceUrl;

    @Value("${goci.url}")
    private String oldCurationUrl;

    @Value("${gwas-deposition-service.fileupload.endpoints.submission}")
    private String submissionEndpoint;

    @Value("${gwas-curation-service.javers.endpoints.javers-submission}")
    private String javersEndpoint;

    @Value("${goci.diseaseTraits.endpoints.reported-traits-upload}")
    private String oldDiseaseTraitsUploadEndpoint;


    @Value("${goci.diseaseTraits.endpoints.reported-traits}")
    private String oldDiseaseTraitsEndpoint;

    @Value("${goci.diseaseTraits.endpoints.search-reported-traits}")
    private String oldDiseaseTraitsSearchEndpoint;

    public String getDepositionServiceUrl() {
        return depositionServiceUrl;
    }

    public String getSubmissionEndpoint() { return submissionEndpoint; }

    public String getJaversEndpoint() { return javersEndpoint; }

    public String getCurationServiceUrl() {
        return curationServiceUrl;
    }

    public String getOldDiseaseTraitsEndpoint() {
        return oldDiseaseTraitsEndpoint;
    }

    public String getOldCurationUrl() {
        return oldCurationUrl;
    }

    public String getOldDiseaseTraitsUploadEndpoint() {
        return oldDiseaseTraitsUploadEndpoint;
    }

    public String getOldDiseaseTraitsSearchEndpoint() {
        return oldDiseaseTraitsSearchEndpoint;
    }
}
