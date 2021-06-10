package uk.ac.ebi.spot.gwas.curation.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ValueChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.rest.dto.SampleDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.JaversCommonService;
import uk.ac.ebi.spot.gwas.curation.service.SampleJaversService;
import uk.ac.ebi.spot.gwas.curation.service.SamplesService;
import uk.ac.ebi.spot.gwas.deposition.domain.Sample;
import uk.ac.ebi.spot.gwas.deposition.dto.SampleDto;
import uk.ac.ebi.spot.gwas.deposition.javers.AddedRemoved;
import uk.ac.ebi.spot.gwas.deposition.javers.ElementChange;
import uk.ac.ebi.spot.gwas.deposition.javers.ValueChangeWrapper;
import uk.ac.ebi.spot.gwas.deposition.javers.VersionDiffStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SampleJaversServiceImpl implements SampleJaversService {

    private static final Logger log = LoggerFactory.getLogger(SampleJaversServiceImpl.class);

    @Autowired
    JaversCommonService javersCommonService;

    @Autowired
    SamplesService samplesService;

    public VersionDiffStats findSampleChanges(List<Sample> prevSamples, List<Sample> newSamples, VersionDiffStats diffStats) {

        if(!newSamples.isEmpty())
            diffStats.setSampleGroups(new ArrayList<>());

        prevSamples.forEach((sample) -> {
            log.info("Sample*****"+sample.getStage()+"|"+sample.getAncestryCategory());
            List<SampleDto> newSamplesDto = newSamples.stream()
                    .filter((sampleGroup) -> (sampleGroup.getStage() + sampleGroup.getAncestryCategory())
                            .equals(sample.getStage() + sample.getAncestryCategory()))
                    .map(SampleDtoAssembler::assemble)
                    .collect(Collectors.toList());
            SampleDto prevSampleDto = SampleDtoAssembler.assemble(sample);
            if (!newSamplesDto.isEmpty()) {
                List<ValueChangeWrapper> valChanges = diffSamples(newSamplesDto.get(0), prevSampleDto);
                if(!valChanges.isEmpty()) {
                    VersionDiffStats versionDiffStats = new VersionDiffStats();
                    versionDiffStats.setEntity(sample.getStage()+"|"+sample.getAncestryCategory());
                    versionDiffStats.setEdited(valChanges.stream().
                            map(javersCommonService::mapChangetoVersionStats)
                            .collect(Collectors.toList()));
                    diffStats.getSampleGroups().add(versionDiffStats);
                }
            }

        });

        return diffStats;


    }

    public List<ValueChangeWrapper> diffSamples(SampleDto dto1, SampleDto dto2) {
        Javers javers = JaversBuilder.javers().build();
        Diff diff = javers.compare(dto1, dto2);
        log.info("************");
        log.info("Diff Sample"+ diff);
        List<ValueChange> valChanges = diff.getChangesByType(ValueChange.class);
        try {
            ValueChangeWrapper[]  changes = new ObjectMapper().readValue(
                    javers.getJsonConverter().toJson(valChanges), ValueChangeWrapper[].class);
            return Arrays.asList(changes);
        } catch(Exception ex){
            log.error("Error in mapping Javers Changes"+ex.getMessage(),ex );
            return null;
        }
    }

    public AddedRemoved getSampleVersionStats(List<Sample> prevSamples, List<Sample> newSamples) {
        log.info("Inside getAssociationVersionStats() ");

        List<String> newSamplesTags = newSamples.stream()
                .map(sample -> sample.getStudyTag() + sample.getStage() +sample.getAncestryCategory())
                .collect(Collectors.toList());

        List<String> prevSamplesTags = prevSamples.stream()
                .map(sample -> sample.getStudyTag() + sample.getStage() +sample.getAncestryCategory())
                .collect(Collectors.toList());

        List<Sample> samplesRemoved = prevSamples.stream()
                .filter(sample -> !newSamplesTags.contains(sample.getStudyTag() + sample.getStage() +
                        sample.getAncestryCategory()))
                .collect(Collectors.toList());

        List<Sample> samplesAdded = newSamples.stream()
                .filter(sample -> !prevSamplesTags.contains(sample.getStudyTag() + sample.getStage() +
                        sample.getAncestryCategory()))
                .collect(Collectors.toList());

        log.info("newSamplesTags****"+newSamplesTags);
        log.info("prevSamplesTags****"+prevSamplesTags);
        log.info("samplesRemoved****"+samplesRemoved);
        log.info("samplesAdded****"+samplesAdded);

        AddedRemoved addedRemoved = new AddedRemoved();
        addedRemoved.setAdded(samplesAdded.size());
        addedRemoved.setRemoved(samplesRemoved.size());

        return addedRemoved;
    }

    public Sample processSampleTag(ElementChange elementChange) {
        if (elementChange.getElementChangeType().equals("ValueAdded")){
            return samplesService.getSample(elementChange.getValue().toString() );
        }
        return null;
    }
}
