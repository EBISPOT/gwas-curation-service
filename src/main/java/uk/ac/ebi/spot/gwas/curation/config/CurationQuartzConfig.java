package uk.ac.ebi.spot.gwas.curation.config;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.spot.gwas.curation.scheduler.config.WeeklyCurationQueueStatsConfig;
import uk.ac.ebi.spot.gwas.curation.scheduler.config.WeeklyCurationStatusSnapshotConfig;
import uk.ac.ebi.spot.gwas.curation.scheduler.config.WeeklyPublicationStatsConfig;
import uk.ac.ebi.spot.gwas.deposition.scheduler.config.QuartzSchedulerJobConfig;

import javax.annotation.PostConstruct;

@Configuration
public class CurationQuartzConfig {

    @Autowired
    private QuartzSchedulerJobConfig quartzSchedulerJobConfig;

    @Autowired(required = false)
    private WeeklyPublicationStatsConfig weeklyPublicationStatsConfig;

    @Autowired(required = false)
    private WeeklyCurationStatusSnapshotConfig weeklyCurationStatusSnapshotConfig;

    @Autowired(required = false)
    private WeeklyCurationQueueStatsConfig weeklyCurationQueueStatsConfig;

    @PostConstruct
    private void initialize() throws SchedulerException {
        if(weeklyPublicationStatsConfig != null) {
            quartzSchedulerJobConfig.addJob(weeklyPublicationStatsConfig);
        }
        if(weeklyCurationStatusSnapshotConfig != null) {
            quartzSchedulerJobConfig.addJob(weeklyCurationStatusSnapshotConfig);
        }
        if(weeklyCurationQueueStatsConfig != null ){
            quartzSchedulerJobConfig.addJob(weeklyCurationQueueStatsConfig);
        }
        quartzSchedulerJobConfig.initializeJobs();

    }
}
