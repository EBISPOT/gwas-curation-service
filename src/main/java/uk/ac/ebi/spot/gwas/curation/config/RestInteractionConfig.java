package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RestInteractionConfig {

    @Value("${gwas-deposition-service.url}")
    private String depositionServiceUrl;

    @Value("${gwas-curation-service.url}")
    private String curationServiceUrl;

    @Value("${gwas-deposition-service.fileupload.endpoints.submission}")
    private String submissionEndpoint;

    @Value("${gwas-curation-service.javers.endpoints.javers-submission}")
    private String javersEndpoint;

    @Value("${europepmc.root}")
    private String euroPMCEndpoint;

    @Value("${europepmc.search.pubmed}")
    private String euroPMCSearchUrl;

    @Value("${gwas-ingest-service.url}")
    private String ingestServiceUri;

    @Value("${gwas-ingest-service.endpoints.submission-envelope}")
    private String submissionEnvelopeEndpoint;

    @Value("${audit.service.uri}")
    private String auditServiceUri;

    @Value("${audit.endpoints.publication}")
    private String  auditServicePublicationEndpoint;

    @Value("${audit.endpoints.pub-audit-entries}")
    private String  auditServicePubAuditeEntriesEndpoint;

    public String getDepositionServiceUrl() {
        return depositionServiceUrl;
    }

    public String getSubmissionEndpoint() { return submissionEndpoint; }

    public String getJaversEndpoint() { return javersEndpoint; }

    public String getCurationServiceUrl() {
        return curationServiceUrl;
    }

    public String getEuroPMCEndpoint() {
        return euroPMCEndpoint;
    }

    public String getEuroPMCSearchUrl() {
        return euroPMCSearchUrl;
    }

    public String getIngestServiceUri() {
        return ingestServiceUri;
    }

    public String getSubmissionEnvelopeEndpoint() {
        return submissionEnvelopeEndpoint;
    }

    public String getAuditServiceUri() {
        return auditServiceUri;
    }

    public String getAuditServicePublicationEndpoint() {
        return auditServicePublicationEndpoint;
    }

    public String getAuditServicePubAuditeEntriesEndpoint() {
        return auditServicePubAuditeEntriesEndpoint;
    }
}
