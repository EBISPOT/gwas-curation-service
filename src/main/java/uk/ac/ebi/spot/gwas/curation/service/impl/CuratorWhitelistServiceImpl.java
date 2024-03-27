package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.repository.CuratorWhitelistRepository;
import uk.ac.ebi.spot.gwas.curation.service.CuratorWhitelistService;
import uk.ac.ebi.spot.gwas.deposition.domain.CuratorWhitelist;

import java.util.Optional;

@Service
public class CuratorWhitelistServiceImpl implements CuratorWhitelistService {

    private static final Logger log = LoggerFactory.getLogger(CuratorWhitelistServiceImpl.class);
    @Autowired
    CuratorWhitelistRepository curatorWhitelistRepository;

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @Override
    public Boolean isCuratorWhiteListed(String email) {
        if(depositionCurationConfig.getCuratorAuthMechanism() != null &&
                depositionCurationConfig.getCuratorAuthMechanism().equals(DepositionCurationConstants.EMAIL_WHITE_LIST)) {
            log.info("Curator whitelist block");
            Optional<CuratorWhitelist> optionalCuratorWhitelist = curatorWhitelistRepository.findByEmailIgnoreCase(email);
            log.info("Curator is whitelisted {}",optionalCuratorWhitelist.isPresent());
            return optionalCuratorWhitelist.isPresent();
        }
        return false;
    }
}
