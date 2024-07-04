package uk.ac.ebi.spot.gwas.curation.config.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.ac.ebi.spot.gwas.curation.service.CuratorWhitelistService;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    CuratorWhitelistService curatorWhitelistService;

    private static final Logger log = LoggerFactory.getLogger(AuthTokenFilter.class);
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        this.setHeaders(response);
        try {
            String jwt = CurationUtil.parseJwt(request);
            String email = getEmailFromJwtToken(jwtUtils.getClaims(jwt));
            if(jwt != null && (jwt.equalsIgnoreCase("SpringRestDocsDummyToken") || curatorWhitelistService.isCuratorWhiteListed(email))) {
                log.info("Inside Bypass authentication Block");
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_self.GWAS_Curator"));
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("", null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
            else if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                UsernamePasswordAuthenticationToken authentication = this.getAuthFromJwtToken(jwtUtils.getClaims(jwt));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }



    private UsernamePasswordAuthenticationToken getAuthFromJwtToken(Claims claims) {
        AapPayload aapPayload = mapper.convertValue(claims, new TypeReference<AapPayload>() {
        });
        List<String> roles = aapPayload.convertAapDomainsToSpringSecurityRoles();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        return new UsernamePasswordAuthenticationToken("", null, authorities);
    }

    private String getEmailFromJwtToken(Claims claims) {
        AapPayload aapPayload = mapper.convertValue(claims, new TypeReference<AapPayload>() {
        });
        return aapPayload.getEmail();
    }




    private void setHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "authorization, content-type, xsrf-token");
        response.addHeader("Access-Control-Expose-Headers", "xsrf-token");
    }

}