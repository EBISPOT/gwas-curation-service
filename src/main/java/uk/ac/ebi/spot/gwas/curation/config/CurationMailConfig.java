package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CurationMailConfig {
    @Value("${gwas-curation.email-config.emails.publicationweeklystats.subject}")
    private String publicationWeeklySubject;

    @Value("${gwas-curation.email-config.emails.publicationweeklystats.success}")
    private String publicationWeeklySuccessEmail;

    @Value("${gwas-curation.email-config.emails.curationsnapshotstats.success}")
    private String publicationCurationSnapshotSuccessEmail;

    @Value("${gwas-curation.email-config.emails.curationsnapshotstats.subject}")
    private String publicationCurationStatsSubject;


    @Value("${gwas-curation.email-config.emails.curationqueue.success}")
    private String publicationCurationQueueSuccessEmail;

    @Value("${gwas-curation.email-config.emails.curationqueue.subject}")
    private String publicationCurationQueueSubject;


    @Value("${gwas-curation.email-config.to-address}")
    private String toAddress;



    @Value("${gwas-curation.email-config.base-url}")
    private String submissionsBaseURL;


    @Value("${gwas-curation.email-config.docs-url}")
    private String submissionsDocsURL;


    @Value("${gwas-curation.email-config.errors.subject}")
    private String errorsSubject;

    @Value("${gwas-curation.email-config.errors.email}")
    private String errorsEmail;

    @Value("${gwas-curation.email-config.errors.receiver}")
    private String errorsReceiver;

    @Value("${gwas-curation.email-config.errors.active}")
    private boolean errorsActive;

    public String getSubmissionsBaseURL() {
        return submissionsBaseURL;
    }

    public String getPublicationWeeklySubject() {
        return publicationWeeklySubject;
    }

    public String getPublicationWeeklySuccessEmail() {
        return publicationWeeklySuccessEmail;
    }


    public boolean isErrorsActive() {
        return errorsActive;
    }

    public String getErrorsEmail() {
        return errorsEmail;
    }

    public String getErrorsSubject() {
        return errorsSubject;
    }


    public String getSubmissionsDocsURL() {
        return submissionsDocsURL;
    }


    public String getPublicationCurationSnapshotSuccessEmail() {
        return publicationCurationSnapshotSuccessEmail;
    }

    public String getToAddress() {
        return toAddress;
    }

    public String getPublicationCurationStatsSubject() {
        return publicationCurationStatsSubject;
    }

    public String getPublicationCurationQueueSuccessEmail() {
        return publicationCurationQueueSuccessEmail;
    }

    public String getPublicationCurationQueueSubject() {
        return publicationCurationQueueSubject;
    }

    public List<String> getErrorsReceiver() {
        List<String> result = new ArrayList<>();
        String[] parts = errorsReceiver.split(",");
        for (String part : parts) {
            result.add(part.trim());
        }
        return result;
    }
}
