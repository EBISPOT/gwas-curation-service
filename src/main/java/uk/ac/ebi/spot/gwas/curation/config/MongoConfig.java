package uk.ac.ebi.spot.gwas.curation.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.repository.mongo.MongoRepository;
import org.javers.spring.auditable.AuthorProvider;
import org.javers.spring.auditable.EmptyPropertiesProvider;
import org.javers.spring.auditable.aspect.springdata.JaversSpringDataAuditableRepositoryAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.ac.ebi.spot.gwas.curation.util.SimpleAuthorProvider;
import uk.ac.ebi.spot.gwas.deposition.config.SystemConfigProperties;

import java.util.ArrayList;
import java.util.List;

public class MongoConfig {

    @Configuration
    @EnableMongoRepositories(basePackages = {"uk.ac.ebi.spot.gwas.curation.repository"})
    @EnableTransactionManagement
    @Profile({"dev", "test", "local"})
    public static class MongoConfigDev extends AbstractMongoConfiguration {

        @Autowired
        private SystemConfigProperties systemConfigProperties;

        @Override
        protected String getDatabaseName() {
            String serviceName = systemConfigProperties.getServerName();
            String environmentName = systemConfigProperties.getActiveSpringProfile();
            return serviceName + "-" + environmentName;
        }

        @Bean
        public GridFsTemplate gridFsTemplate() throws Exception {
            return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
        }

        @Override
        public MongoClient mongoClient() {
            String mongoUri = systemConfigProperties.getMongoUri();
            String[] addresses = mongoUri.split(",");
            List<ServerAddress> servers = new ArrayList<>();
            for (String address : addresses) {
                String[] split = address.trim().split(":");
                servers.add(new ServerAddress(split[0].trim(), Integer.parseInt(split[1].trim())));
            }
            return new MongoClient(servers);
        }

        @Bean
        public Javers javers() {
            MongoRepository mongoRepository = new MongoRepository(mongoClient().getDatabase(getDatabaseName()));
            return JaversBuilder.javers().registerJaversRepository(mongoRepository).build();
        }

        @Bean
        public JaversSpringDataAuditableRepositoryAspect javersSpringDataAuditableAspect() {
            return new JaversSpringDataAuditableRepositoryAspect(javers(), provideJaversAuthor(),
                    new EmptyPropertiesProvider());
        }

        @Bean
        public AuthorProvider provideJaversAuthor(){
            return new SimpleAuthorProvider();
        }

    }

    @Configuration
    @EnableMongoRepositories(basePackages = {"uk.ac.ebi.spot.gwas.curation.repository"})
    @EnableTransactionManagement
    @Profile({"sandbox","sandbox-migration"})
    public static class MongoConfigSandbox extends AbstractMongoConfiguration {

        @Autowired
        private SystemConfigProperties systemConfigProperties;

        @Autowired
        private DepositionCurationConfig depositionCurationConfig;

        @Override
        protected String getDatabaseName() {
            return depositionCurationConfig.getDbName();
        }

        @Bean
        public GridFsTemplate gridFsTemplate() throws Exception {
            return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
        }

        @Override
        public MongoClient mongoClient() {
            String mongoUri = systemConfigProperties.getMongoUri();
            return new MongoClient(new MongoClientURI("mongodb://" + mongoUri));
        }

        @Bean
        public Javers javers() {
            MongoRepository mongoRepository = new MongoRepository(mongoClient().getDatabase(getDatabaseName()));
            return JaversBuilder.javers().registerJaversRepository(mongoRepository).build();
        }

        @Bean
        public JaversSpringDataAuditableRepositoryAspect javersSpringDataAuditableAspect() {
            return new JaversSpringDataAuditableRepositoryAspect(javers(), provideJaversAuthor(),
                    new EmptyPropertiesProvider());
        }

        @Bean
        public AuthorProvider provideJaversAuthor(){
            return new SimpleAuthorProvider();
        }

    }

    @Configuration
    @EnableMongoRepositories(basePackages = {"uk.ac.ebi.spot.gwas.curation.repository"})
    @EnableTransactionManagement
    @Profile({"prod", "prod-fallback"})
    public static class MongoConfigProd extends AbstractMongoConfiguration {

        @Autowired
        private SystemConfigProperties systemConfigProperties;

        @Autowired
        private DepositionCurationConfig depositionCurationConfig;

        @Override
        protected String getDatabaseName() {
            return depositionCurationConfig.getDbName();
        }

        @Bean
        public GridFsTemplate gridFsTemplate() throws Exception {
            return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
        }

        @Override
        public MongoClient mongoClient() {
            String mongoUri = systemConfigProperties.getMongoUri();
            String dbUser = systemConfigProperties.getDbUser();
            String dbPassword = systemConfigProperties.getDbPassword();
            String credentials = "";
            if (dbUser != null && dbPassword != null) {
                dbUser = dbUser.trim();
                dbPassword = dbPassword.trim();
                if (!dbUser.equalsIgnoreCase("") &&
                        !dbPassword.equalsIgnoreCase("")) {
                    credentials = dbUser + ":" + dbPassword + "@";
                }
            }

            return new MongoClient(new MongoClientURI("mongodb://" + credentials + mongoUri));
        }

        @Bean
        public Javers javers() {
            MongoRepository mongoRepository = new MongoRepository(mongoClient().getDatabase(getDatabaseName()));
            return JaversBuilder.javers().registerJaversRepository(mongoRepository).build();
        }

        @Bean
        public JaversSpringDataAuditableRepositoryAspect javersSpringDataAuditableAspect() {
            return new JaversSpringDataAuditableRepositoryAspect(javers(), provideJaversAuthor(),
                    new EmptyPropertiesProvider());
        }

        @Bean
        public AuthorProvider provideJaversAuthor(){
            return new SimpleAuthorProvider();
        }
    }

    @Configuration
    @EnableMongoRepositories(basePackages = {"uk.ac.ebi.spot.gwas.curation.repository"})
    @EnableTransactionManagement
    @Profile({"gcp-sandbox"})
    public static class MongoConfiGCPSandbox extends AbstractMongoConfiguration {

        @Autowired
        private SystemConfigProperties systemConfigProperties;

        @Autowired
        private DepositionCurationConfig depositionCurationConfig;

        @Override
        protected String getDatabaseName() {
            return depositionCurationConfig.getDbName();
        }

        @Bean
        public GridFsTemplate gridFsTemplate() throws Exception {
            return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
        }

        @Override
        public MongoClient mongoClient() {
            String mongoUri = systemConfigProperties.getMongoUri();
            String dbUser = systemConfigProperties.getDbUser();
            String dbPassword = systemConfigProperties.getDbPassword();
            String credentials = "";
            if (dbUser != null && dbPassword != null) {
                dbUser = dbUser.trim();
                dbPassword = dbPassword.trim();
                if (!dbUser.equalsIgnoreCase("") &&
                        !dbPassword.equalsIgnoreCase("")) {
                    credentials = dbUser + ":" + dbPassword + "@";
                }
            }

            return new MongoClient(new MongoClientURI("mongodb+srv://" + credentials + mongoUri));
        }
    }
}
