package uk.ac.ebi.spot.gwas.curation.config.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class AapPayload {

    private String iss;
    private String jti;
    private Integer iat;
    private String sub;
    private String email;
    private String nickname;
    private String name;
    private List<String> domains;
    private Integer exp;

    public List<String> convertAapDomainsToSpringSecurityRoles() {
        return this.domains.stream()
                .map(domain -> "ROLE_" + domain)
                .collect(Collectors.toList());
    }
}
