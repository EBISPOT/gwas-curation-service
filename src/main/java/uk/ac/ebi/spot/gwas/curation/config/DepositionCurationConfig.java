package uk.ac.ebi.spot.gwas.curation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;

import java.util.List;

@Component
public class DepositionCurationConfig {

    @Value("${gwas-curation.auth.enabled}")
    private boolean authEnabled;

    @Value("${gwas-curation.auth.cert:#{NULL}}")
    private String certPath;

    @Value("${gwas-curation.auth.auto-curator-service-account:#{NULL}}")
    private String autoCuratorServiceAccount;

    @Value("${gwas-curation.auth.curators.auth-mechanism:JWT_DOMAIN}")
    private String curatorAuthMechanism;

    @Value("${gwas-curation.auth.curators.jwt-domains:#{NULL}}")
    private String curatorDomains;

    @Value("${gwas-curation.db:#{NULL}}")
    private String dbName;

    public String getDbName() {
        return dbName;
    }

    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public String getCertPath() {
        return certPath;
    }

    public String getAutoCuratorServiceAccount() {
        return autoCuratorServiceAccount;
    }

    public String getCuratorAuthMechanism() {
        return curatorAuthMechanism;
    }

    public List<String> getCuratorDomains() {
        return CurationUtil.sToList(curatorDomains);
    }
}
