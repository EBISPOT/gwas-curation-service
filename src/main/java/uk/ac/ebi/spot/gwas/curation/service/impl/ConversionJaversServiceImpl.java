package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.service.*;
import uk.ac.ebi.spot.gwas.deposition.domain.Association;
import uk.ac.ebi.spot.gwas.deposition.domain.FileUpload;
import uk.ac.ebi.spot.gwas.deposition.domain.Sample;
import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.exception.NoVersionSummaryException;
import uk.ac.ebi.spot.gwas.deposition.javers.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ConversionJaversServiceImpl implements ConversionJaversService {

    private static final Logger log = LoggerFactory.getLogger(ConversionJaversServiceImpl.class);

    @Autowired
    private AssociationJaversService associationJaversService;

    @Autowired
    private StudyJaversService studyJaversService;

    @Autowired
    private SampleJaversService sampleJaversService;

    @Autowired
    private FileUploadJaversService fileUploadJaversService;



    @Override
    public Optional<Map<Double, List<JaversChangeWrapper>>> filterJaversResponse(List<JaversChangeWrapper> javersChangeWrapperList) {
        return Optional.of(javersChangeWrapperList)
                .map((changeList) -> {
                    List<Double> commitIdChanges = changeList.stream()
                            .filter((javersChangeWrapper) ->
                                    ( javersChangeWrapper.getProperty().equals("metadataStatus")) &&
                                    javersChangeWrapper.getRight().toString().equals("VALID"))
                            .map((change) -> change.getCommitMetadata().getId())
                            .collect(Collectors.toList());
                    Map<Double, List<JaversChangeWrapper>> versionMap = new LinkedHashMap<>();
                    commitIdChanges.forEach((commitId) -> {
                      List<JaversChangeWrapper> versionChanges =  changeList.stream()
                              .filter((change) -> change.getCommitMetadata().getId().equals(commitId))
                              .collect(Collectors.toList());
                      versionMap.put(commitId, versionChanges );
                    });
                   return versionMap;
                });

    }
    @Override
    public Optional<List<FileUpload>> filterJaversResponseForFiles(List<JaversChangeWrapper> javersChangeWrapperList) {

        return Optional.of(javersChangeWrapperList)
                .map((changes) ->
                    changes.stream()
                            .filter((change) -> change.getProperty().equals("fileUploads"))
                            .flatMap((change) -> change.getElementChanges().stream())
                            .map(fileUploadJaversService::processFileUploadTag)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()).stream()
                            .distinct()
                            .map(fileUploadJaversService::getFileUploadDetails)
                            .filter(fileUpload -> !fileUpload.getStatus().equals("INVALID"))
                            .collect(Collectors.toList()));




    }

    /**
     * Find indexes in Javers API for changes having sumstatsstus as Invalid
     * & subsequent index will be for metadata submission associated with
     * invalid sumstatst , use this index to fetch the version number which
     * needs to be removed for invalid sumstats
     *
     * @param javersChangeWrapperList
     */
    public List<Double> removeInvalidSumstatsEntries(List<JaversChangeWrapper> javersChangeWrapperList) {

      List<Integer> invalidSumstatsIndexes = IntStream.range(0, javersChangeWrapperList.size())
                .filter(i -> ( javersChangeWrapperList.get(i).getProperty().equals("summaryStatsStatus")
                && javersChangeWrapperList.get(i).getRight().equals("INVALID")))
                .mapToObj(i -> i)
                .collect(Collectors.toList());

        List<Double> versionList = invalidSumstatsIndexes.stream()
                .map(idx -> javersChangeWrapperList.get(idx+1).getCommitMetadata().getId())
                .collect(Collectors.toList());

        versionList.forEach(version  -> log.info(" Version->"+version));

        return versionList;

    }

    public void removeVersionMap(Optional<Map<Double, List<JaversChangeWrapper>>> javersChangeWrapperMap,
                                 List<Double> versionsTobeRemoved ) {
        versionsTobeRemoved.forEach((version) -> javersChangeWrapperMap.get().remove(version));
    }

    @Override
    public List<VersionSummary> filterStudiesFromJavers(Optional<Map<Double, List<JaversChangeWrapper>>> javersChangeWrapperList) {
        Map<Double, List<JaversChangeWrapper>> versionMap = javersChangeWrapperList.orElse(null);
        log.info("versionMap ****"+versionMap);
        if(versionMap != null && !versionMap.isEmpty()){
            if(versionMap.size() < 2 ){
                throw new NoVersionSummaryException("No Version Stats are available currently for this submission," +
                            " Please upload the edited Template file");
            }
        }
        List<VersionSummary> summaries = new ArrayList<>();
        Set<Double> keys = versionMap.keySet();
        log.info("keys ****"+keys);
        Double[] keysArray =  keys.toArray(new Double[keys.size()]);
        log.info("keysArray ****"+keysArray);
        for(int i = 0 ; i < keys.size() -1 ; i++) {
            log.info("Inside Keys");
            VersionSummary versionSummary = compareVersions(versionMap.get(keysArray[i]),
                    versionMap.get(keysArray[i+1]));
            summaries.add(versionSummary);
        }
        return summaries;
    }

    public List<VersionSummary> mapFilesToVersionSummary(List<VersionSummary> summaries, List<FileUpload> fileUploads) {
        VersionSummary[] summaryArr = summaries.toArray(new VersionSummary[summaries.size()]);
        FileUpload[] fileUploadArr = fileUploads.toArray(new FileUpload[fileUploads.size()]);

        for(int i = 0 ; i < summaryArr.length; i++) {
            summaryArr[i].setOldFileDetails(new FileSummaryStats( fileUploadArr[i+1].getFileName(),
                    fileUploadArr[i+1].getId()));
            summaryArr[i].setNewFileDetails(new FileSummaryStats( fileUploadArr[i].getFileName(),
                    fileUploadArr[i].getId()));
        }
        return Arrays.asList(summaryArr);
    }



    private VersionSummary compareVersions(List<JaversChangeWrapper> newChange, List<JaversChangeWrapper> oldChange) {
        List<Study> newStudies = newChange.stream()
                .filter( (javersChangeWrapper) ->
                javersChangeWrapper.getProperty().equals("studies"))
                .flatMap((javersChange) -> javersChange.getElementChanges().stream())
                .map(studyJaversService::processStudyTag)
                .collect(Collectors.toList());

        List<Study> prevStudies = oldChange.stream()
                .filter( (javersChangeWrapper) ->
                javersChangeWrapper.getProperty().equals("studies"))
                .flatMap((javersChange) -> javersChange.getElementChanges().stream())
                .map(studyJaversService::processStudyTag)
                .collect(Collectors.toList());

        List<Association> newAssociations = newChange.stream()
                .filter( (javersChangeWrapper) ->
                javersChangeWrapper.getProperty().equals("associations"))
                .flatMap((javersChange) -> javersChange.getElementChanges().stream())
                .map(associationJaversService::processAssociationTag)
                .collect(Collectors.toList());


        List<Association> prevAssociations = oldChange.stream()
                .filter( (javersChangeWrapper) ->
                javersChangeWrapper.getProperty().equals("associations"))
                .flatMap((javersChange) -> javersChange.getElementChanges().stream())
                .map(associationJaversService::processAssociationTag)
                .collect(Collectors.toList());

        List<Sample> newSamples = newChange.stream()
                .filter( (javersChangeWrapper) ->
                        javersChangeWrapper.getProperty().equals("samples"))
                .flatMap((javersChange) -> javersChange.getElementChanges().stream())
                .map(sampleJaversService::processSampleTag)
                .collect(Collectors.toList());


        List<Sample> prevSamples = oldChange.stream()
                .filter( (javersChangeWrapper) ->
                        javersChangeWrapper.getProperty().equals("samples"))
                .flatMap((javersChange) -> javersChange.getElementChanges().stream())
                .map(sampleJaversService::processSampleTag)
                .collect(Collectors.toList());

        log.info("newStudies****"+newStudies);
        log.info("prevStudies****"+prevStudies);
        log.info("newAssociations****"+newAssociations);
        log.info("prevAssociations****"+prevAssociations);

        VersionSummary versionSummary = new VersionSummary();
        versionSummary.setCurrentVersionSummary(populateCurrentVersionSummary(
                newStudies.size(), newAssociations.size(), newSamples.size()));
        VersionSummaryStats versionSummaryStats = new VersionSummaryStats();
        VersionDiffStats  versionDiffStats = new VersionDiffStats();
        AddedRemoved addedRemoved = studyJaversService.getStudyVersionStats(prevStudies , newStudies, versionDiffStats);

        VersionSummaryStats studyStats = populateVersionSummaryStudyStats(addedRemoved.getAdded(),
                addedRemoved.getRemoved(), versionSummaryStats);


        AddedRemoved addedRemovedasscn = associationJaversService.getAssociationVersionStats(prevAssociations, newAssociations);
        VersionSummaryStats asscnStats = populateVersionSummaryAssociationStats(addedRemovedasscn.getAdded(),
                addedRemovedasscn.getRemoved(), studyStats);

        AddedRemoved addedRemovedSmpl = sampleJaversService.getSampleVersionStats(prevSamples, newSamples);
        VersionSummaryStats sampleStats = populateVersionSummarySampleStats(addedRemovedSmpl.getAdded() ,
                addedRemovedSmpl.getRemoved(), asscnStats);

        AddedRemoved traitsAddedRemoved = studyJaversService.getReportedTraitVersionStats(prevStudies, newStudies);
        VersionSummaryStats traitsStats = populateVersionSummaryTraitsStats(traitsAddedRemoved.getAdded(), traitsAddedRemoved.getRemoved(),
                sampleStats);
        AddedRemoved efosAddedRemoved = studyJaversService.getReportedEfoVersionStats(prevStudies, newStudies);
        VersionSummaryStats efoStats = populateVersionSummaryEfoStats(efosAddedRemoved.getAdded(), efosAddedRemoved.getRemoved(),
                traitsStats);
        versionSummary.setVersionSummaryStats(efoStats);


        versionDiffStats.setStudies(new ArrayList<>());
        Map<String,List<Association>> prevstudyAscnsMap = prevAssociations.stream()
                .collect(Collectors.groupingBy(Association::getStudyTag));
        Map<String,List<Association>> newstudyAscnsMap = newAssociations.stream()
                .collect(Collectors.groupingBy(Association::getStudyTag));
        Map<String,List<Sample>> prevStudySamplesMap = prevSamples.stream()
                .collect(Collectors.groupingBy(Sample::getStudyTag));
        Map<String,List<Sample>> newStudySamplesMap = newSamples.stream()
                .collect(Collectors.groupingBy(Sample::getStudyTag));
        Map<String, List<Study>> prevStudyMap = prevStudies.stream()
                .collect(Collectors.groupingBy(Study::getStudyTag));
        Map<String, List<Study>> newStudyMap = newStudies.stream()
                .collect(Collectors.groupingBy(Study::getStudyTag));


        prevStudyMap.forEach((tag, studyList) -> {
            log.info("Study Tag ****"+tag);
            VersionDiffStats  versionStudyDiffStats = studyJaversService.findStudyChanges(tag, studyList, newStudies);
            if(prevstudyAscnsMap.get(tag) != null  ) {
                log.info("Inside Association loop ");
                AddedRemoved addedRemovedAsscns = associationJaversService.getAssociationVersionStats(prevstudyAscnsMap.get(tag),
                        newstudyAscnsMap.get(tag) !=null ? newstudyAscnsMap.get(tag) : Collections.emptyList());

                versionStudyDiffStats.setAscnsAdded(addedRemovedAsscns.getAdded());
                versionStudyDiffStats.setAscnsRemoved(addedRemovedAsscns.getRemoved());
                versionStudyDiffStats = associationJaversService.findAssociationChanges(tag, prevstudyAscnsMap.get(tag),
                        newstudyAscnsMap.get(tag) !=null ? newstudyAscnsMap.get(tag) : Collections.emptyList(), versionStudyDiffStats);

            } else {
                if (newstudyAscnsMap.get(tag) != null) {
                    log.info("Inside Association loop where old study has no asscn ");
                    AddedRemoved addedRemovedAsscns = associationJaversService.getAssociationVersionStats(Collections.emptyList(),
                            newstudyAscnsMap.get(tag) );
                    versionStudyDiffStats.setAscnsAdded(addedRemovedAsscns.getAdded());
                    versionStudyDiffStats.setAscnsRemoved(addedRemovedAsscns.getRemoved());
                } else {
                    AddedRemoved addedRemovedAsscns = associationJaversService.getAssociationVersionStats(Collections.emptyList(),
                            Collections.emptyList() );
                    versionStudyDiffStats.setAscnsAdded(addedRemovedAsscns.getAdded());
                    versionStudyDiffStats.setAscnsRemoved(addedRemovedAsscns.getRemoved());
                }
            }

            if(prevStudySamplesMap.get(tag) != null  ) {
                log.info("Inside Sample loop ");
                AddedRemoved addedRemovedSamples = sampleJaversService.getSampleVersionStats(prevStudySamplesMap.get(tag), newStudySamplesMap.get(tag) !=null ?
                        newStudySamplesMap.get(tag) : Collections.emptyList());
                versionStudyDiffStats.setSamplesAdded(addedRemovedSamples.getAdded());
                versionStudyDiffStats.setSamplesRemoved(addedRemovedSamples.getRemoved());
                versionStudyDiffStats = sampleJaversService.findSampleChanges( prevStudySamplesMap.get(tag), newStudySamplesMap.get(tag) !=null ?
                        newStudySamplesMap.get(tag) : Collections.emptyList(), versionStudyDiffStats);
            } else {
                if(newStudySamplesMap.get(tag) != null) {
                    log.info("Inside Study loop where old study has no Sample ");
                    AddedRemoved addedRemovedSamples = sampleJaversService.getSampleVersionStats(Collections.emptyList(), newStudySamplesMap.get(tag) !=null ?
                            newStudySamplesMap.get(tag) : Collections.emptyList());
                    versionStudyDiffStats.setSamplesAdded(addedRemovedSamples.getAdded());
                    versionStudyDiffStats.setSamplesRemoved(addedRemovedSamples.getRemoved());
                }else {
                    AddedRemoved addedRemovedSamples = sampleJaversService.getSampleVersionStats(Collections.emptyList(),
                            Collections.emptyList() );
                    versionStudyDiffStats.setSamplesAdded(addedRemovedSamples.getAdded());
                    versionStudyDiffStats.setSamplesRemoved(addedRemovedSamples.getRemoved());
                }
            }
            versionDiffStats.getStudies().add(versionStudyDiffStats);
        });

        String[] studyTagsAdded = versionDiffStats.getStudyTagsAdded().split(",");
        List<String> studyTagsList = Arrays.asList(studyTagsAdded);

        newStudyMap.forEach((tag, studyList) -> {
            if(studyTagsList != null && studyTagsList.contains(tag)) {
                log.info("Studies added newly");
                log.info("Studies added ->"+tag);
                VersionDiffStats newversionDiffStats = new VersionDiffStats();
                newversionDiffStats.setEntity(tag);
                AddedRemoved addedRemovedAsscns = associationJaversService.getAssociationVersionStats(Collections.emptyList(),
                        newstudyAscnsMap.get(tag) !=null ?
                                newstudyAscnsMap.get(tag) : Collections.emptyList());
                AddedRemoved addedRemovedSamples = sampleJaversService.getSampleVersionStats(Collections.emptyList(),
                        newStudySamplesMap.get(tag) !=null ?
                        newStudySamplesMap.get(tag) : Collections.emptyList());
                newversionDiffStats.setAscnsAdded(addedRemovedAsscns.getAdded());
                newversionDiffStats.setAscnsRemoved(addedRemovedAsscns.getRemoved());
                newversionDiffStats.setSamplesAdded(addedRemovedSamples.getAdded());
                newversionDiffStats.setSamplesRemoved(addedRemovedSamples.getRemoved());
                versionDiffStats.getStudies().add(newversionDiffStats);
            }
        });

        versionSummary.setVersionDiffStats(versionDiffStats);
        return versionSummary;
    }

    private CurrentVersionSummary populateCurrentVersionSummary(int countStudies, int countAscns, int  countSamples) {
        CurrentVersionSummary currentVersionSummary = new CurrentVersionSummary();
        currentVersionSummary.setTotalStudies(countStudies);
        currentVersionSummary.setTotalAssociations(countAscns);
        currentVersionSummary.setTotalSamples(countSamples);
        return currentVersionSummary;
    }

    private VersionSummaryStats populateVersionSummaryStudyStats(int added, int removed, VersionSummaryStats stats) {
        stats.setStudiesAdded(added);
        stats.setStudiesRemoved(removed);
        return stats;
    }

    private VersionSummaryStats populateVersionSummaryTraitsStats(int added, int removed, VersionSummaryStats stats) {
        stats.setReportedTraitsAdded(added);
        stats.setReportedTraitsRemoved(removed);
        return stats;
    }

    private VersionSummaryStats populateVersionSummaryEfoStats(int added, int removed, VersionSummaryStats stats) {
        stats.setEfoTraitsAdded(added);
        stats.setEfoTraitsRemoved(removed);
        return stats;
    }

    private VersionSummaryStats populateVersionSummaryAssociationStats(int added, int removed, VersionSummaryStats stats) {
        stats.setAscnsAdded(added);
        stats.setAscnsRemoved(removed);
        return stats;
    }

    private VersionSummaryStats populateVersionSummarySampleStats(int added, int removed, VersionSummaryStats stats) {
        stats.setSamplesAdded(added);
        stats.setSamplesRemoved(removed);
        return stats;
    }













}
