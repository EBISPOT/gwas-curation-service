package uk.ac.ebi.spot.gwas.curation.util;

import org.javers.spring.auditable.AuthorProvider;

public class SimpleAuthorProvider implements AuthorProvider {

    @Override
    public String provide() {
        return "Javers-Audit";
    }
}
