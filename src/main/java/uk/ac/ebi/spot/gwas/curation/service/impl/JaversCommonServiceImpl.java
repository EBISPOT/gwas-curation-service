package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.service.JaversCommonService;
import uk.ac.ebi.spot.gwas.deposition.javers.DiffPropertyObject;
import uk.ac.ebi.spot.gwas.deposition.javers.ValueChangeWrapper;

@Service
public class JaversCommonServiceImpl implements JaversCommonService {

    public  DiffPropertyObject mapChangetoVersionStats(ValueChangeWrapper valueChangeWrapper) {
        DiffPropertyObject diffStats = new DiffPropertyObject();
        diffStats.setProperty(valueChangeWrapper.getProperty());
        diffStats.setOldValue(valueChangeWrapper.getLeft());
        diffStats.setNewValue(valueChangeWrapper.getRight());
        return diffStats;

    }
}
