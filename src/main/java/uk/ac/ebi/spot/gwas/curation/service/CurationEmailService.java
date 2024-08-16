package uk.ac.ebi.spot.gwas.curation.service;

import java.io.File;
import java.util.Map;

public interface CurationEmailService {

    void sendWeeklyPublicationStatsMail(Map<String, Object> metadata, File attach);

    void sendWeeklyCurationSnapshotStatsMail(Map<String, Object> metadata, File attach);

    void sendWeeklyCurationQueueStatsMail(Map<String, Object> metadata, File attach);


}
