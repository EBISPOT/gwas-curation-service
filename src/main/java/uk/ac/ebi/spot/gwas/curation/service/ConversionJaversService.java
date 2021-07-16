package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.FileUpload;
import uk.ac.ebi.spot.gwas.deposition.javers.JaversChangeWrapper;
import uk.ac.ebi.spot.gwas.deposition.javers.VersionSummary;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ConversionJaversService {

    public Optional<Map<Double, List<JaversChangeWrapper>>> filterJaversResponse(List<JaversChangeWrapper> javersChangeWrapperList);

    public List<Double> removeInvalidSumstatsEntries(List<JaversChangeWrapper> javersChangeWrapperList);

    public void removeVersionMap(Optional<Map<Double, List<JaversChangeWrapper>>> javersChangeWrapperMap,
                                 List<Double> versionsTobeRemoved );

    public Optional<List<FileUpload>> filterJaversResponseForFiles(List<JaversChangeWrapper> javersChangeWrapperList);

    public List<VersionSummary> filterStudiesFromJavers(Optional<Map<Double, List<JaversChangeWrapper>>> javersChangeWrapperList);

    public List<VersionSummary> mapFilesToVersionSummary(List<VersionSummary> summaries, List<FileUpload> fileUploads);
}
