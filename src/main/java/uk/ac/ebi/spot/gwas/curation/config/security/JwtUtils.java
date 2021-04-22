package uk.ac.ebi.spot.gwas.curation.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtils {

    private ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    @Autowired
    private DepositionCurationConfig depositionCurationConfig;

    private PublicKey verifyingKey;

    @PostConstruct
    public void initialize() {
        log.info("Initializing auth cert. Auth enabled: {}", depositionCurationConfig.isAuthEnabled());
        if (depositionCurationConfig.isAuthEnabled()) {
            String certPath = depositionCurationConfig.getCertPath();
            log.info("Using cert: {}", certPath);
            if (certPath == null) {
                log.error("Unable to initialize cert. Path is NULL.");
            } else {
                try {
                    InputStream inputStream = new DefaultResourceLoader().getResource(certPath).getInputStream();
                    final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                    final X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
                    verifyingKey = certificate.getPublicKey();
                } catch (Exception e) {
                    log.error("Unable to initialize cert: {}", e.getMessage(), e);
                }
            }
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser().setSigningKey(verifyingKey).parseClaimsJws(token).getBody();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(verifyingKey).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }


}