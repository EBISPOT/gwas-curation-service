package uk.ac.ebi.spot.gwas.curation.service;

public interface CuratorWhitelistService {

    Boolean isCuratorWhiteListed(String email);
}
