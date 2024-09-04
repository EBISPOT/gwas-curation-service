package uk.ac.ebi.spot.gwas.curation.scheduler.config;

import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.spot.gwas.curation.scheduler.job.WeeklyCurationStatusSnapshotJob;
import uk.ac.ebi.spot.gwas.curation.scheduler.job.WeeklyPublicationStatsJob;
import uk.ac.ebi.spot.gwas.deposition.scheduler.config.AbstractQuartzConfig;

import java.util.Date;

@Configuration
public class WeeklyCurationStatusSnapshotConfig extends AbstractQuartzConfig {

    private static final String JK_WCURATIONSTATUS_STATS = "JK_WCURATIONSTATUS_STATS";

    private static final String PG_WCURATIONSTATUS_STATS = "PG_WCURATIONSTATUS_STATS";

    private static final String TK_WCURATIONSTATUS_STATS = "TK_WCURATIONSTATUS_STATS";


    @Value("${quartz.jobs.weekly-curationstatus-stats.schedule}")
    private String weeklyCurationStatusSchedule;


    public WeeklyCurationStatusSnapshotConfig() {
        super(TK_WCURATIONSTATUS_STATS, PG_WCURATIONSTATUS_STATS);
    }

    public JobDetail getjobDetail() {
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setKey(new JobKey(JK_WCURATIONSTATUS_STATS, PG_WCURATIONSTATUS_STATS));
        jobDetail.setJobClass(WeeklyCurationStatusSnapshotJob.class);
        jobDetail.setDurability(true);
        return jobDetail;
    }

    public Trigger createNewTrigger(Date startTime) {
        return TriggerBuilder.newTrigger()
                .forJob(this.getjobDetail())
                .withIdentity(TK_WCURATIONSTATUS_STATS, PG_WCURATIONSTATUS_STATS)
                .withPriority(50)
                .withSchedule(CronScheduleBuilder.cronSchedule(weeklyCurationStatusSchedule))
                .startAt(startTime).build();
    }
}
