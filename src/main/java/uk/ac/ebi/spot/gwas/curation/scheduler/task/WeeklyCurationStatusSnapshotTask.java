package uk.ac.ebi.spot.gwas.curation.scheduler.task;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.service.CurationEmailService;
import uk.ac.ebi.spot.gwas.curation.service.FtpService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationAuditEntryService;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.audit.CurationStatusSnapshotStats;
import uk.ac.ebi.spot.gwas.deposition.audit.PublicationWeeklyStats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class WeeklyCurationStatusSnapshotTask {

    private static final Logger log = LoggerFactory.getLogger(WeeklyPublicationStatsTask.class);

    private static final String STATS_WEEK = "STATS_WEEK";

    private PublicationAuditEntryService publicationAuditEntryService;

    String path = "";
    FileHandler fileHandler;

    CurationEmailService curationEmailService;

    FtpService ftpService;

    @Autowired
    public WeeklyCurationStatusSnapshotTask(PublicationAuditEntryService publicationAuditEntryService,
                                            FileHandler fileHandler,
                                            CurationEmailService curationEmailService,
                                            FtpService ftpService) {
        this.publicationAuditEntryService = publicationAuditEntryService;
        this.fileHandler = fileHandler;
        this.curationEmailService = curationEmailService;
        this.ftpService = ftpService;
        path = CurationUtil.getDefaultClassPath();
    }


    public void buildStats() {
        log.info("Inside buildStats of WeeklyCurationStatusSnapshotTask");
        int dayOfYear = DateTime.now().dayOfYear().get();
        DateTime aWeekAgo = DateTime.now().minusDays(dayOfYear);
        CurationStatusSnapshotStats curationStatusSnapshotStats = publicationAuditEntryService.getCurationStatusSnapshotStats(aWeekAgo);
        FileOutputStream fos;
        String formattedWeeklyDate = CurationUtil.getCurrentDate();
        log.info("the formatted weekly date is {}", formattedWeeklyDate);
        String filename = String.format("WeeklyCurationStatusSnapshot_%s%s", formattedWeeklyDate, ".tsv");
        log.info("the filename as email attachement is {}", filename);
        File weeklyFile = null;
        log.info("Running Publication weekly stats for {}", formattedWeeklyDate);
        try {
            log.info("The classoath is  {}", path);
            int lastIdx = path.lastIndexOf("/");
            String weeklyFilepath = path.substring(0, lastIdx + 1) + filename;
            log.info("The weeklyFilepath is  {}", weeklyFilepath);
            weeklyFile = new File(weeklyFilepath);
            if (!weeklyFile.exists()) {
                weeklyFile.createNewFile();
            }
            fos = new FileOutputStream(weeklyFile);
            fos.write(fileHandler.serializePojoToTsv(Collections.singletonList(curationStatusSnapshotStats)));
        } catch (IOException ex) {
            log.error("IO Exception in file wrting", ex.getMessage(), ex);
        }
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(STATS_WEEK, formattedWeeklyDate);
        curationEmailService.sendWeeklyCurationSnapshotStatsMail(metadata, weeklyFile);
        ftpService.uploadWeeklyStasFiles(weeklyFile);
        weeklyFile.delete();
    }
}