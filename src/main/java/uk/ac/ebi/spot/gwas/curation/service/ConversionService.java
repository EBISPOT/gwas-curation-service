package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.javers.JaversChangeWrapper;

import java.util.List;
import java.util.Optional;

public interface ConversionService {

    public Optional<List<JaversChangeWrapper>> filterJaversResponse(List<JaversChangeWrapper> javersChangeWrapperList);

    //public void filterStudiesFromJavers(List<JaversChangeWrapper> javersChangeWrapperList);
}
