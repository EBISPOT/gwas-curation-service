package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.javers.DiffPropertyObject;
import uk.ac.ebi.spot.gwas.deposition.javers.ValueChangeWrapper;

public interface JaversCommonService {
    public DiffPropertyObject mapChangetoVersionStats(ValueChangeWrapper valueChangeWrapper);
}
