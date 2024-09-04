package uk.ac.ebi.spot.gwas.curation.scheduler.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import uk.ac.ebi.spot.gwas.curation.scheduler.task.WeeklyCurationQueueTask;
import uk.ac.ebi.spot.gwas.curation.scheduler.task.WeeklyCurationStatusSnapshotTask;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class WeeklyCurationQueueJob extends QuartzJobBean {

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        applicationContext.getBean(WeeklyCurationQueueTask.class).buildStats();
    }
}
