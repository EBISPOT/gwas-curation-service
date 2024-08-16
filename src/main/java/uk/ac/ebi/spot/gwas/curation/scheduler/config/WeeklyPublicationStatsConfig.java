package uk.ac.ebi.spot.gwas.curation.scheduler.config;

import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.spot.gwas.curation.scheduler.job.WeeklyPublicationStatsJob;
import uk.ac.ebi.spot.gwas.deposition.scheduler.config.AbstractQuartzConfig;

import java.util.Date;
@Configuration
public class WeeklyPublicationStatsConfig extends AbstractQuartzConfig {

    private static final String JK_WPUBCHANGES_STATS = "JK_WPUBCHANGES_STATS";

    private static final String PG_WPUBCHANGES_STATS = "PG_WPUBCHANGES_STATS";

    private static final String TK_WPUBCHANGES_STATS = "TK_WPUBCHANGES_STATS";

    @Value("${quartz.jobs.weekly-publication-stats.schedule}")
    private String weeklyPublicationStatsSchedule;

    public WeeklyPublicationStatsConfig() {
        super(TK_WPUBCHANGES_STATS, PG_WPUBCHANGES_STATS);
    }

    public JobDetail getjobDetail() {
        JobDetailImpl jobDetail = new JobDetailImpl();
        jobDetail.setKey(new JobKey(JK_WPUBCHANGES_STATS, PG_WPUBCHANGES_STATS));
        jobDetail.setJobClass(WeeklyPublicationStatsJob.class);
        jobDetail.setDurability(true);
        return jobDetail;
    }

    public Trigger createNewTrigger(Date startTime) {
        return TriggerBuilder.newTrigger()
                .forJob(this.getjobDetail())
                .withIdentity(TK_WPUBCHANGES_STATS, PG_WPUBCHANGES_STATS)
                .withPriority(50)
                .withSchedule(CronScheduleBuilder.cronSchedule(weeklyPublicationStatsSchedule))
                .startAt(startTime).build();
    }
}
