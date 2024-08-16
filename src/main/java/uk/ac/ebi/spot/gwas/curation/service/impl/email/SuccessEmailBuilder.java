package uk.ac.ebi.spot.gwas.curation.service.impl.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import uk.ac.ebi.spot.gwas.deposition.messaging.email.AbstractEmailBuilder;
import uk.ac.ebi.spot.gwas.deposition.messaging.email.EmailBuilder;

import java.util.Map;

public class SuccessEmailBuilder extends AbstractEmailBuilder implements EmailBuilder {

    private static final Logger log = LoggerFactory.getLogger(SuccessEmailBuilder.class);

    public SuccessEmailBuilder(String emailFile) {
        super(emailFile);
    }

    @Override
    public String getEmailContent(Map<String, Object> metadata) {
        log.info("Building success email from: {}", emailFile);
        String content = super.readEmailContent();
        //log.info("Email Content is {}",content );
        if (content != null) {
            Context context = new Context();
            log.info("Email Context context start");
            for (String variable : metadata.keySet()) {
                log.info("Variable in context {}", variable);

                Object variableValue = metadata.get(variable);
                log.info("variableValue in context {}", variableValue);
                context.setVariable(variable, variableValue);
            }
            log.info("Email Context context End");
            try {
             String emaiTemplate =  templateEngine.process(content, context);
                //log.info("Email Template parsing  {}",emaiTemplate);
             return emaiTemplate;
            }catch(Exception ex) {
                log.error("Exception in template parsing for email",ex.getMessage(),ex);
            }catch(Throwable throwable){
                log.error("Throwable in template process", throwable.getMessage(),throwable);
            }
        }
        return null;
    }
}
