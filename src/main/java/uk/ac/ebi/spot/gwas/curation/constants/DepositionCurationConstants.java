package uk.ac.ebi.spot.gwas.curation.constants;

public class DepositionCurationConstants {

    public static final String API_TEST = "/test";
    public static final String API_SUBMISSIONS = "/submissions";
    public static final String API_STUDIES = "/studies";
    public static final String API_ASSOCIATIONS = "/associations";
    public static final String API_SAMPLEDESCRIPTION = "/sampledescription";
    public static final String API_STUDIES_TRAITS_UPLOAD = "/studies/fileupload";
    public static final String API_EDIT_UPLOADS = "/uploads/edit";
    public static final String API_SUBMISSIONS_LOCK = "/lock";
    public static final String API_SUBMISSION_VERSION = "/versions";
    public static final String API_JAVERS = "/javers";
    public static final String API_JAVERS_CHANGES = "/changes";
    public static final String API_DISEASE_TRAITS = "/reported-traits";
    public static final String API_DISEASE_TRAITS_FILE_UPLOAD = "/reported-traits/files";
    public static final String API_LITERATURE_FILES = "/literature-files";
    public static final String PARAM_TRAIT = "trait";
    public static final String PARAM_STUDY_ID = "studyId";
    public static final String PARAM_SUBMISSION_ID = "submissionId";
    public static final String LINKS_PARENT = "parent";
    public static final String LINKS_DISEASE_TRAITS = "diseaseTraits";
    public static final String API_EFO_TRAITS = "/efo-traits";
    public static final String API_MULTI_TRAITS = "/multi-traits";
    public static final String PARAM_PMID = "pmid";
    public static final String PARAM_BOWID = "bowId";
    public static final String PARAM_META_STATUS = "metaStatus";
    public static final String PARAM_SS_STATUS = "ssStatus";
    public static final String PARAM_SUBMISSION_STATUS = "submissionStatus";
    public static final String PARAM_LOCK_STATUS = "lockStatus";
    public static final String ANALYSIS_STATUS_PROCESSING = "PROCESSING";
    public static final String ANALYSIS_STATUS_DONE = "DONE";
    public static final String STUDY_COLLECTION = "studies";
    public static final String API_INGEST_STUDIES = "/ingest-studies";
    public static final String API_SOLR_STUDIES ="/solr/studies";
    public static final String API_POPULATE_SOLR_STUDIES = "/reindex-studies";
    public static final String QUEUE_NAME_SANDBOX = "study_change_sandbox";
    public static final String QUEUE_NAME_PROD = "study_change";

    public static final String QUEUE_NAME_SUMSTATS_SANDBOX = "metadata-yml-update-sandbox";
    public static final String EXCHANGE_NAME = "study_change_exchange";
    public static final String ROUTING_KEY = "study-ingest";
    public static final String API_SOLR_REMOVE = "/remove-studies";
    public static final String API_PUBLICATIONS = "/publications";
    public static final String API_BODY_OF_WORK = "/body-of-work";
    public static final String API_PUBLICATIONS_SOLR = "/publications/search";
    public static final String API_CURATORS = "/curators";
    public static final String API_CURATION_STATUS = "/curation-status";

    public static final String EMAIL_WHITE_LIST = "EMAIL_WHITELISTING";


    public static final String API_PUBLICATION = "/publications";
    public static final String API_PUBLICATION_AUDIT_ENTRIES = "/publication-audit-entries";

    public static final String  PARAM_PUBID = "publicationId";

}
