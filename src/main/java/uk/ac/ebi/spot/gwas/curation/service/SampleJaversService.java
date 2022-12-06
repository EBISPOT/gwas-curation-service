package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.Sample;
import uk.ac.ebi.spot.gwas.deposition.dto.SampleDto;
import uk.ac.ebi.spot.gwas.deposition.javers.AddedRemoved;
import uk.ac.ebi.spot.gwas.deposition.javers.ElementChange;
import uk.ac.ebi.spot.gwas.deposition.javers.ValueChangeWrapper;
import uk.ac.ebi.spot.gwas.deposition.javers.VersionDiffStats;

import java.util.List;

public interface SampleJaversService {

    public VersionDiffStats findSampleChanges(List<Sample> prevSamples, List<Sample> newSamples, VersionDiffStats diffStats);

    public List<ValueChangeWrapper> diffSamples(SampleDto dto1, SampleDto dto2);

    public AddedRemoved getSampleVersionStats(List<Sample> prevSamples, List<Sample> newSamples);

    public String processSampleTag(ElementChange elementChange);


}
