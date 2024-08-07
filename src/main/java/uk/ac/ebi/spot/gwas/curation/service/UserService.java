package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.User;

public interface UserService {

    User findUser(User user, boolean createIfNotExistent);

    User getUser(String userId);

    User findUserDetailsUsingEmail(String email);
}
