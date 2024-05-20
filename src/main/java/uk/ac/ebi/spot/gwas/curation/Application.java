package uk.ac.ebi.spot.gwas.curation;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.ac.ebi.spot.gwas.deposition.config.SystemConfigProperties;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication(scanBasePackages = "uk.ac.ebi.spot.gwas")
@EnableScheduling
@EnableAsync
@EnableCaching
@OpenAPIDefinition(info = @Info(title = "Curation Service API", version = "1.0", description = "Curation Service Microservices"))
public class Application implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private SystemConfigProperties systemConfigProperties;

    @PostConstruct
    public void init() {
        log.info("[{}] Initializing: {}", DateTime.now(), systemConfigProperties.getServerName());
    }

    @PreDestroy
    public void destroy() {
        log.info("[{}] Shutting down: {}", DateTime.now(), systemConfigProperties.getServerName());
    }

    public static void main(String[] args) throws UnknownHostException {
        log.info("Inside Curation Application");
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        String logFileName = System.getenv(GeneralCommon.LOG_FILE_NAME);
        System.setProperty("log.file.name", logFileName + "-" + hostAddress);
        SpringApplication.run(Application.class, args);
    }
}
