package uk.ac.ebi.spot.gwas.curation.scheduler.config;

import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.spot.gwas.curation.scheduler.job.WeeklyCurationQueueJob;
import uk.ac.ebi.spot.gwas.curation.scheduler.job.WeeklyCurationStatusSnapshotJob;
import uk.ac.ebi.spot.gwas.deposition.scheduler.config.AbstractQuartzConfig;

import java.util.Date;

@Configuration
public class WeeklyCurationQueueStatsConfig extends AbstractQuartzConfig {

    private static final String JK_WCURATIONQUEUE_STATS = "JK_WCURATIONQUEUE_STATS";

    private static final String PG_WCURATIONQUEUE_STATS = "PG_WCURATIONQUEUE_STATS";

    private static final String TK_WCURATIONQUEUE_STATS = "TK_WCURATIONQUEUE_STATS";


    @Value("${quartz.jobs.weekly-curationqueue-stats.schedule}")
    private String weeklyCurationQueueSchedule;

    public WeeklyCurationQueueStatsConfig() {
        super(TK_WCURATIONQUEUE_STATS, PG_WCURATIONQUEUE_STATS);
    }

    public JobDetail getjobDetail() {
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setKey(new JobKey(JK_WCURATIONQUEUE_STATS, PG_WCURATIONQUEUE_STATS));
        jobDetail.setJobClass(WeeklyCurationQueueJob.class);
        jobDetail.setDurability(true);
        return jobDetail;
    }

    public Trigger createNewTrigger(Date startTime) {
        return TriggerBuilder.newTrigger()
                .forJob(this.getjobDetail())
                .withIdentity(TK_WCURATIONQUEUE_STATS, PG_WCURATIONQUEUE_STATS)
                .withPriority(50)
                .withSchedule(CronScheduleBuilder.cronSchedule(weeklyCurationQueueSchedule))
                .startAt(startTime).build();
    }
}
