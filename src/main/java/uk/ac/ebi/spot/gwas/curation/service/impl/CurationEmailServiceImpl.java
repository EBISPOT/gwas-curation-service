package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.config.CurationMailConfig;
import uk.ac.ebi.spot.gwas.curation.service.CurationEmailService;
import uk.ac.ebi.spot.gwas.curation.service.impl.email.SuccessEmailBuilder;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.messaging.email.EmailBuilder;
import uk.ac.ebi.spot.gwas.deposition.messaging.email.EmailService;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

@Service
public class CurationEmailServiceImpl implements CurationEmailService {

    @Autowired
    CurationMailConfig curationMailConfig;

    @Autowired
    EmailService emailService;

    public void sendWeeklyPublicationStatsMail(Map<String, Object> metadata, File attach) {
        EmailBuilder successEmailBuilder = new SuccessEmailBuilder(curationMailConfig.getPublicationWeeklySuccessEmail());
        for(String address : Arrays.asList(curationMailConfig.getToAddress()
                .split(","))){
            emailService.sendMessageWithAttachments(address, getWeeklyPublicationSubject(),  successEmailBuilder.getEmailContent(metadata), attach, false );
        }

    }


    public void sendWeeklyCurationSnapshotStatsMail(Map<String, Object> metadata, File attach) {
        EmailBuilder successEmailBuilder = new SuccessEmailBuilder(curationMailConfig.getPublicationCurationSnapshotSuccessEmail());
        for(String address : Arrays.asList(curationMailConfig.getToAddress()
                .split(","))){
            emailService.sendMessageWithAttachments(address, getWeeklyCurationStatsSubject(),  successEmailBuilder.getEmailContent(metadata), attach, false );
        }

    }

    public void sendWeeklyCurationQueueStatsMail(Map<String, Object> metadata, File attach) {
        EmailBuilder successEmailBuilder = new SuccessEmailBuilder(curationMailConfig.getPublicationCurationQueueSuccessEmail());
        for(String address : Arrays.asList(curationMailConfig.getToAddress()
                .split(","))){
            emailService.sendMessageWithAttachments(address, getWeeklyCurationQueueSubject(),  successEmailBuilder.getEmailContent(metadata), attach, false );
        }
    }

    private String getWeeklyPublicationSubject() {
      String subject =  curationMailConfig.getPublicationWeeklySubject();
        String formattedWeeklyDate = CurationUtil.getCurrentDate();
      if(subject.contains("%WEEK%")) {
          subject = subject.replace("%WEEK%", formattedWeeklyDate );
      }
      return subject;
    }


    private String getWeeklyCurationStatsSubject() {
        String subject =  curationMailConfig.getPublicationCurationStatsSubject();
        String formattedWeeklyDate = CurationUtil.getCurrentDate();
        if(subject.contains("%WEEK%")) {
            subject = subject.replace("%WEEK%", formattedWeeklyDate );
        }
        return subject;
    }

    private String getWeeklyCurationQueueSubject() {
        String subject =  curationMailConfig.getPublicationCurationQueueSubject();
        String formattedWeeklyDate = CurationUtil.getCurrentDate();
        if(subject.contains("%WEEK%")) {
            subject = subject.replace("%WEEK%", formattedWeeklyDate );
        }
        return subject;
    }

}
