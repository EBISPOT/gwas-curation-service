package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.User;

public interface JWTService {

    User extractUser(String jwt);
}
