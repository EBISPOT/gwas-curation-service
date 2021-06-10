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
import uk.ac.ebi.spot.gwas.curation.rest.dto.StudyDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.JaversCommonService;
import uk.ac.ebi.spot.gwas.curation.service.StudiesService;
import uk.ac.ebi.spot.gwas.curation.service.StudyJaversService;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.dto.StudyDto;
import uk.ac.ebi.spot.gwas.deposition.javers.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudyJaversServiceImpl implements StudyJaversService {

    private static final Logger log = LoggerFactory.getLogger(StudyJaversServiceImpl.class);

    @Autowired
    StudiesService studiesService;

    @Autowired
    JaversCommonService javersCommonService;

    public VersionDiffStats findStudyChanges(String tag, List<Study> studyList, List<Study> newStudies) {

        List<StudyDto> newStudiesDTO = newStudies.stream()
                .filter((study) -> study.getStudyTag().equals(tag))
                .map(StudyDtoAssembler::assemble)
                .collect(Collectors.toList());

        log.info("Inside findStudyChanges newStudie**** ");

        List<StudyDto> prevStudiesDTO = studyList.stream()
                .map(StudyDtoAssembler::assemble)
                .collect(Collectors.toList());


        VersionDiffStats versionDiffStats = new VersionDiffStats();
        versionDiffStats.setEntity(tag);
        if (!newStudiesDTO.isEmpty()) {
            List<ValueChangeWrapper> studyChanges = diffStudies(prevStudiesDTO.get(0),
                    newStudiesDTO.get(0));
            if (!studyChanges.isEmpty())
                versionDiffStats.setEdited(studyChanges.stream()
                        .map(javersCommonService::mapChangetoVersionStats)
                        .collect(Collectors.toList()));

        }
        return versionDiffStats;

    }



    public List<ValueChangeWrapper> diffStudies(StudyDto dto1, StudyDto dto2) {
        Javers javers = JaversBuilder.javers().build();
        Diff diff = javers.compare(dto1, dto2);
        log.info("************");
        log.info("Diff" + diff);
        List<ValueChange> valChanges = diff.getChangesByType(ValueChange.class);
        try {
            ValueChangeWrapper[] changes = new ObjectMapper().readValue(
                    javers.getJsonConverter().toJson(valChanges), ValueChangeWrapper[].class);
            return Arrays.asList(changes);
        } catch (Exception ex) {
            log.error("Error in mapping Javers Changes" + ex.getMessage(), ex);
            return null;
        }
    }

    public AddedRemoved getStudyVersionStats(List<Study> prevStudies, List<Study> newStudies, VersionDiffStats versionDiffStats) {
        List<String> newStudyTags = newStudies.stream()
                .map(Study::getStudyTag)
                .collect(Collectors.toList());

        List<String> prevStudyTags = prevStudies.stream()
                .map(Study::getStudyTag)
                .collect(Collectors.toList());

        List<Study> studiesRemoved = prevStudies.stream()
                .filter((study) -> !newStudyTags.contains(study.getStudyTag()))
                .collect(Collectors.toList());

        String studyTagsRemoved = studiesRemoved.stream()
                .map(Study::getStudyTag)
                .collect(Collectors.joining(","));

        List<Study> studiesAdded = newStudies.stream()
                .filter((study) -> !prevStudyTags.contains(study.getStudyTag()))
                .collect(Collectors.toList());

        String studyTagsAdded = studiesAdded.stream()
                .map(Study::getStudyTag)
                .collect(Collectors.joining(","));


        log.info("newStudyTags****" + newStudyTags);
        log.info("prevStudyTags****" + prevStudyTags);
        log.info("studiesRemoved****" + studiesRemoved);
        log.info("studiesAdded****" + studiesAdded);

        versionDiffStats.setStudyTagsAdded(studyTagsAdded);
        versionDiffStats.setStudyTagsRemoved(studyTagsRemoved);

        AddedRemoved addedRemoved = new AddedRemoved();
        addedRemoved.setAdded(studiesAdded.size());
        addedRemoved.setRemoved(studiesRemoved.size());
        return addedRemoved;

    }

    public AddedRemoved getReportedTraitVersionStats(List<Study> prevStudies, List<Study> newStudies) {

        log.info("Inside getReportedTraitVersionStats()");

        List<String> newReportedTraits = newStudies.stream()
                .map(Study::getTrait)
                .collect(Collectors.toList());

        List<String> prevReportedTraits = prevStudies.stream()
                .map(Study::getTrait)
                .collect(Collectors.toList());

        List<Study> traitsRemoved = prevStudies.stream()
                .filter((study) -> !newReportedTraits.contains(study.getTrait()))
                .collect(Collectors.toList());

        List<Study> traitsAdded = newStudies.stream()
                .filter((study) -> !prevReportedTraits.contains(study.getTrait()))
                .collect(Collectors.toList());

        log.info("newReportedTraits****"+newReportedTraits);
        log.info("prevReportedTraits****"+prevReportedTraits);
        log.info("traitsRemoved****"+traitsRemoved);
        log.info("traitsAdded****"+traitsAdded);

        AddedRemoved addedRemoved = new AddedRemoved();
        addedRemoved.setAdded(traitsAdded.size());
        addedRemoved.setRemoved(traitsRemoved.size());
        return addedRemoved;

    }


    public AddedRemoved getReportedEfoVersionStats(List<Study> prevStudies, List<Study> newStudies) {

        log.info("Inside getReportedEfoVersionStats()");
        List<String> newEfoTraits = newStudies.stream()
                .map(Study::getEfoTrait)
                .flatMap((efos) -> Arrays.stream(efos.split("\\|")))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        List<String> prevEfoTraits = prevStudies.stream()
                .map(Study::getEfoTrait)
                .flatMap((efos) -> Arrays.stream(efos.split("\\|")))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

       /* List<Study> efoRemoved = prevStudies.stream()
                .filter((study) -> !newEfoTraits.contains(study.getTrait()))
                .collect(Collectors.toList());*/

        List<String> efoRemoved = prevStudies.stream()
                .flatMap(study -> Arrays.stream(study.getEfoTrait().split("\\|")))
                .map(String::trim)
                .filter(efo -> !newEfoTraits.contains(efo))
                .distinct()
                .collect(Collectors.toList());


        /*List<Study> efoAdded = newStudies.stream()
                .filter((study) -> !prevEfoTraits.contains(study.getTrait()))
                .collect(Collectors.toList());*/
        List<String> efoAdded = newStudies.stream()
                .flatMap(study -> Arrays.stream(study.getEfoTrait().split("\\|")))
                .map(String::trim)
                .filter(efo -> !prevEfoTraits.contains(efo))
                .distinct()
                .collect(Collectors.toList());

        log.info("newEfoTraits****" + newEfoTraits);
        log.info("prevEfoTraits****" + prevEfoTraits);
        log.info("efoRemoved****" + efoRemoved);
        log.info("efoAdded****" + efoAdded);
        AddedRemoved addedRemoved = new AddedRemoved();
        addedRemoved.setAdded(efoAdded.size());
        addedRemoved.setRemoved(efoRemoved.size());
        return addedRemoved;

    }

    public Study processStudyTag(ElementChange elementChange) {
        if (elementChange.getElementChangeType().equals("ValueAdded")) {
            return studiesService.getStudy(elementChange.getValue().toString());
        }
        return null;
    }

}

