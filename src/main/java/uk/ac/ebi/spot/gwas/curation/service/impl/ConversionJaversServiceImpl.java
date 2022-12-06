package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.atomic.AtomicReference;
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

    @Autowired
    private StudiesService  studiesService;

    @Autowired
    private AssociationsService associationsService;

    @Autowired
    private SamplesService samplesService;


    /**
     * Look for file upload events from Javers response
     * & capture all the versionIds for the events
     * use these versionIds to retrieve the studies,
     * & other entities , the version id will be key
     * for map & events as the value when the file upload
     * happens
     * @param javersChangeWrapperList
     * @return
     */
    @Override
    public Optional<Map<Double, List<JaversChangeWrapper>>> filterJaversResponse(List<JaversChangeWrapper> javersChangeWrapperList) {
        return Optional.of(javersChangeWrapperList)
                .map((changeList) -> {
                    List<Double> commitIdChanges = changeList.stream()
                            .filter((javersChangeWrapper) ->
                                    ( javersChangeWrapper.getProperty().equals("metadataStatus")) &&
                                            javersChangeWrapper.getLeft().toString().equals("VALIDATING") &&
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

    /** The file details events are not saved in the same event
     * as file upload
     * so have to parse Javers response for file details
     * & programmetically  associate them with the specific
     * version
     *
     * @param javersChangeWrapperList
     * @return
     */
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
     * & subsequent index  for metadata submission associated with
     * invalid sumstatst , use this index to fetch the version number which
     * needs to be removed for invalid sumstats
     *
     * @param javersChangeWrapperList
     */
    public List<Double> removeInvalidSumstatsEntries(List<JaversChangeWrapper> javersChangeWrapperList) {

      List<Integer> invalidSumstatsIndexes = IntStream.range(0, javersChangeWrapperList.size())
                .filter(i -> ( javersChangeWrapperList.get(i).getProperty().equals("summaryStatsStatus")
                && ( javersChangeWrapperList.get(i).getRight().equals("INVALID")
                     //|| javersChangeWrapperList.get(i).getLeft().equals("INVALID")
                )))
                //&& javersChangeWrapperList.get(i).getLeft().toString().equals("VALIDATING")))
                .mapToObj(i -> i)
                .collect(Collectors.toList());

        List<Double> versionList = new ArrayList<>();

        if(invalidSumstatsIndexes != null && !invalidSumstatsIndexes.isEmpty()) {
            for (int i = 0; i < invalidSumstatsIndexes.size(); i++) {
                int idx = invalidSumstatsIndexes.get(i);
                versionList.add(javersChangeWrapperList.get(idx).getCommitMetadata().getId());
                for (int j = idx; j < javersChangeWrapperList.size(); j++) {
                //for (int j = idx; j >= 0; j--) {
                    if (javersChangeWrapperList.get(j).getProperty().equals("metadataStatus") &&
                            javersChangeWrapperList.get(j).getLeft().toString().equals("VALIDATING") &&
                            javersChangeWrapperList.get(j).getRight().toString().equals("VALID")) {
                        versionList.add(javersChangeWrapperList.get(j).getCommitMetadata().getId());
                        break;
                    }
                }
            }
        }

/*        List<Double> versionList = invalidSumstatsIndexes.stream()
                .map(idx -> javersChangeWrapperList.get(idx+1).getCommitMetadata().getId())
                .collect(Collectors.toList());*/

        versionList.forEach(version  -> log.info(" Version->"+version));

        return versionList;

    }

    /**
     * Handle Scenarios where the entities are updated
     * possibly due to frequent submission resets
     * this create duplicate copies of same fileupload
     * events , fetch the version for those & remove
     * them from final versionid events which will be
     * used for comparison
     * @param javersChangeWrapperMap
     * @return
     */
    @Override
    public Optional<Set<Double>> removeDuplicateMetaDataVersions(Optional<Map<Double, List<JaversChangeWrapper>>> javersChangeWrapperMap) {

        Set<Double> commitIdstoDelete = new HashSet<>();

        return javersChangeWrapperMap.map(javersChangeMap -> {
                    javersChangeMap.forEach((key, changes) ->
                            changes.forEach(change -> {
                                if (change.getProperty().equals("overallStatus") &&  change.getLeft().toString().equals("VALIDATING") &&
                                        ( change.getRight().toString().equals("SUBMITTED") || change.getRight().toString().equals("CURATION_COMPLETE") || change.getRight().toString().equals("DEPOSITION_COMPLETE"))) {
                                    commitIdstoDelete.add(change.getCommitMetadata().getId());
                                    log.info("Version which is duplicated for Metadata -:"+change.getCommitMetadata().getId());
                                }


                            }));

                    return commitIdstoDelete;
                });


    }

    /**
     * Remove Invalid sumstats events from versions map
     * @param javersChangeWrapperMap
     * @param versionsTobeRemoved
     */
    @Override
    public void removeVersionMap(Optional<Map<Double, List<JaversChangeWrapper>>> javersChangeWrapperMap,
                                 List<Double> versionsTobeRemoved ) {
        versionsTobeRemoved.forEach((version) -> javersChangeWrapperMap.get().remove(version));
    }

    /**
     * Remove Duplicate metadata evets from versions map
     * @param javersChangeWrapperMap
     * @param versionsTobeRemoved
     */
    @Override
    public void removeVersionMap(Optional<Map<Double, List<JaversChangeWrapper>>> javersChangeWrapperMap,
                                 Set<Double> versionsTobeRemoved ) {
        versionsTobeRemoved.forEach((version) -> javersChangeWrapperMap.get().remove(version));
    }

    /**
     * Calls the compare version method for
     * file upload events for each successive
     * file uploads
     * @param javersChangeWrapperList
     * @return
     */
    @Override
    public List<VersionSummary> buiildVersionSummary(Optional<Map<Double, List<JaversChangeWrapper>>> javersChangeWrapperList) {
        Map<Double, List<JaversChangeWrapper>> versionMap = javersChangeWrapperList.orElse(null);
        log.info("versionMap ****"+versionMap);



        if(versionMap != null && !versionMap.isEmpty()){
            if(versionMap.size() < 2 ){
                throw new NoVersionSummaryException("No Version Stats are available currently for this submission," +
                            " Please upload the edited Template file");
            }
        } else {
            throw new NoVersionSummaryException("No Version Stats are available currently for this submission," +
                    " Since the submission has been reset");
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

    /**
     * The file details need to programmetically
     * assigned to versions map as the file details are
     * not saved in the same event for submission upload
     * @param summaries
     * @param fileUploads
     * @return
     */
    public List<VersionSummary> mapFilesToVersionSummary(List<VersionSummary> summaries, List<FileUpload> fileUploads) {
        VersionSummary[] summaryArr = summaries.toArray(new VersionSummary[summaries.size()]);
        FileUpload[] fileUploadArr = fileUploads.toArray(new FileUpload[fileUploads.size()]);

        log.info("VersionSummary length ->"+summaryArr.length);
        log.info("fileUploadArr length ->"+fileUploadArr.length);

       /* if(summaryArr.length != (fileUploadArr.length - 1) )
            throw new NoVersionSummaryException("No Version Stats are available currently for this submission," +
                    " Since the submission has been reset");*/

        if(summaryArr.length > (fileUploadArr.length - 1) ){
            List<VersionSummary> updatedsummaryArr = new ArrayList<>();
            for(int i = 0 ; i < fileUploadArr.length - 1; i++) {
                summaryArr[i].setOldFileDetails(new FileSummaryStats( fileUploadArr[i+1].getFileName(),
                        fileUploadArr[i+1].getId()));
                summaryArr[i].setNewFileDetails(new FileSummaryStats( fileUploadArr[i].getFileName(),
                        fileUploadArr[i].getId()));
                updatedsummaryArr.add(summaryArr[i]);
            }

            return updatedsummaryArr;
        } else {

            for (int i = 0; i < summaryArr.length; i++) {
                summaryArr[i].setOldFileDetails(new FileSummaryStats(fileUploadArr[i + 1].getFileName(),
                        fileUploadArr[i + 1].getId()));
                summaryArr[i].setNewFileDetails(new FileSummaryStats(fileUploadArr[i].getFileName(),
                        fileUploadArr[i].getId()));

            }
            return Arrays.asList(summaryArr);
        }

    }


    /**
     * Compare version events list events
     * Fetch Study & other entity based on SeqId
     * in the events
     * Group studies, association & samples
     * based on study tags for each version
     * then Build version summary object comparing
     * the entities associated with each study tags
     * also the reported Traits & efo traits stats
     * are computed
     * @param newChange
     * @param oldChange
     * @return
     */
    private VersionSummary compareVersions(List<JaversChangeWrapper> newChange, List<JaversChangeWrapper> oldChange) {
        List<String> newStudiesIds = newChange.stream()
                .filter( (javersChangeWrapper) ->
                javersChangeWrapper.getProperty().equals("studies"))
                .flatMap((javersChange) -> javersChange.getElementChanges().stream())
                .map(studyJaversService::processStudyTag)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        List<Study> newStudies = studiesService.getStudies(newStudiesIds).filter(Objects::nonNull)
                .collect(Collectors.toList());


        List<String> prevStudiesIds = oldChange.stream()
                .filter( (javersChangeWrapper) ->
                javersChangeWrapper.getProperty().equals("studies"))
                .flatMap((javersChange) -> javersChange.getElementChanges().stream())
                .map(studyJaversService::processStudyTag)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        List<Study> prevStudies = studiesService.getStudies(prevStudiesIds).filter(Objects::nonNull)
                .collect(Collectors.toList());


        List<String> newAssociationsIds = newChange.stream()
                .filter( (javersChangeWrapper) ->
                javersChangeWrapper.getProperty().equals("associations"))
                .flatMap((javersChange) -> javersChange.getElementChanges().stream())
                .map(associationJaversService::processAssociationTag)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        List<Association> newAssociations = associationsService.readBySeqIds(newAssociationsIds).filter(Objects::nonNull)
                .collect(Collectors.toList());



        List<String> prevAssociationsIds = oldChange.stream()
                .filter( (javersChangeWrapper) ->
                javersChangeWrapper.getProperty().equals("associations"))
                .flatMap((javersChange) -> javersChange.getElementChanges().stream())
                .map(associationJaversService::processAssociationTag)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Association> prevAssociations = associationsService.readBySeqIds(prevAssociationsIds).filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<String> newSamplesIds = newChange.stream()
                .filter( (javersChangeWrapper) ->
                        javersChangeWrapper.getProperty().equals("samples"))
                .flatMap((javersChange) -> javersChange.getElementChanges().stream())
                .map(sampleJaversService::processSampleTag)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Sample> newSamples =   samplesService.findByIdIn(newSamplesIds).filter(Objects::nonNull)
                    .collect(Collectors.toList());


        List<String> prevSamplesIds = oldChange.stream()
                .filter( (javersChangeWrapper) ->
                        javersChangeWrapper.getProperty().equals("samples"))
                .flatMap((javersChange) -> javersChange.getElementChanges().stream())
                .map(sampleJaversService::processSampleTag)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Sample> prevSamples =   samplesService.findByIdIn(prevSamplesIds).filter(Objects::nonNull)
                .collect(Collectors.toList());

        //log.info("newStudies****"+newStudies);
        //log.info("prevStudies****"+prevStudies);
        //log.info("newAssociations****"+newAssociations);
        //log.info("prevAssociations****"+prevAssociations);

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
            //log.info("Study Tag ****"+tag);
            VersionDiffStats  versionStudyDiffStats = studyJaversService.findStudyChanges(tag, studyList, newStudies);
            if(prevstudyAscnsMap.get(tag) != null  ) {
                //log.info("Inside Association loop ");
                AddedRemoved addedRemovedAsscns = associationJaversService.getAssociationVersionStats(prevstudyAscnsMap.get(tag),
                        newstudyAscnsMap.get(tag) !=null ? newstudyAscnsMap.get(tag) : Collections.emptyList());

                versionStudyDiffStats.setAscnsAdded(addedRemovedAsscns.getAdded());
                versionStudyDiffStats.setAscnsRemoved(addedRemovedAsscns.getRemoved());
                versionStudyDiffStats = associationJaversService.findAssociationChanges(tag, prevstudyAscnsMap.get(tag),
                        newstudyAscnsMap.get(tag) !=null ? newstudyAscnsMap.get(tag) : Collections.emptyList(), versionStudyDiffStats);

            } else {
                if (newstudyAscnsMap.get(tag) != null) {
                    //log.info("Inside Association loop where old study has no asscn ");
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
                //log.info("Inside Sample loop ");
                AddedRemoved addedRemovedSamples = sampleJaversService.getSampleVersionStats(prevStudySamplesMap.get(tag), newStudySamplesMap.get(tag) !=null ?
                        newStudySamplesMap.get(tag) : Collections.emptyList());
                versionStudyDiffStats.setSamplesAdded(addedRemovedSamples.getAdded());
                versionStudyDiffStats.setSamplesRemoved(addedRemovedSamples.getRemoved());
                versionStudyDiffStats = sampleJaversService.findSampleChanges( prevStudySamplesMap.get(tag), newStudySamplesMap.get(tag) !=null ?
                        newStudySamplesMap.get(tag) : Collections.emptyList(), versionStudyDiffStats);
            } else {
                if(newStudySamplesMap.get(tag) != null) {
                    //log.info("Inside Study loop where old study has no Sample ");
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
                //log.info("Studies added ->"+tag);
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
