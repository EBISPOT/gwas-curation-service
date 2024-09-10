package uk.ac.ebi.spot.gwas.curation.scheduler.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.service.CurationEmailService;
import uk.ac.ebi.spot.gwas.curation.service.FtpService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationAuditEntryService;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.audit.CurationQueueStats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WeeklyCurationQueueTask {

    private static final Logger log = LoggerFactory.getLogger(WeeklyPublicationStatsTask.class);

    private static final String STATS_WEEK = "STATS_WEEK";

    private PublicationAuditEntryService publicationAuditEntryService;

    String path = "";
    FileHandler fileHandler;

    CurationEmailService curationEmailService;

    FtpService ftpService;

    DepositionCurationConfig depositionCurationConfig;

    @Autowired
    public WeeklyCurationQueueTask(PublicationAuditEntryService publicationAuditEntryService,
                                   FileHandler fileHandler,
                                   CurationEmailService curationEmailService,
                                   FtpService ftpService,
                                   DepositionCurationConfig depositionCurationConfig) {
        this.publicationAuditEntryService = publicationAuditEntryService;
        this.fileHandler = fileHandler;
        this.curationEmailService = curationEmailService;
        this.ftpService = ftpService;
        this.depositionCurationConfig = depositionCurationConfig;
    }

    public void buildStats() {
        log.info("Inside buildStats of WeeklyCurationQueueTask");
        path = depositionCurationConfig.getClassPathResource();
        List<CurationQueueStats> curationQueueStats = publicationAuditEntryService.getCurationQueueStats();
        FileOutputStream fos;
        String formattedWeeklyDate = CurationUtil.getCurrentDate();
        String filename = String.format("CurationQueueStats_%s%s", formattedWeeklyDate, ".tsv");
        log.info("the filename as email attachement is {}", filename);
        File weeklyFile = null;
        log.info("Running Publication weekly stats for {}", formattedWeeklyDate);
        try {
            log.info("Classpath is {}", path);
            int lastIdx = path.lastIndexOf("/");
            String weeklyFilepath = path.substring(0, lastIdx + 1) + filename;
            log.info("The weeklyFilepath is  {}", weeklyFilepath);
            weeklyFile = new File(weeklyFilepath);
            if (!weeklyFile.exists()) {
                weeklyFile.createNewFile();
            }
            fos = new FileOutputStream(weeklyFile);
            fos.write(fileHandler.serializePojoToTsv(curationQueueStats));

        } catch (IOException ex) {
            log.error("IO Exception in file wrting", ex.getMessage(), ex);
        }
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(STATS_WEEK, formattedWeeklyDate);
        curationEmailService.sendWeeklyCurationQueueStatsMail(metadata, weeklyFile);
        ftpService.uploadWeeklyStasFiles(weeklyFile);
        weeklyFile.delete();
    }
}
