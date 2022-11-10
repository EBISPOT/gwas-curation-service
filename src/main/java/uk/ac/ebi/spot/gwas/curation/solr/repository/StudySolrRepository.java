package uk.ac.ebi.spot.gwas.curation.solr.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import uk.ac.ebi.spot.gwas.curation.solr.domain.StudySolr;

import java.util.Optional;

public interface StudySolrRepository extends SolrCrudRepository<StudySolr, String> {

    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND notes:\"*?2*\" AND sumstatsFlag:?3 OR gxeFlag:?4 OR pooledFlag:?5 &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndNotesAndSumstatsFlagOrGxeFlagOrPooledFlag
            (String efoTrait, String reportedTrait, String notes,
             boolean sumstatsFlag, boolean gxeFlag, boolean pooledFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND notes:\"*?2*\" AND sumstatsFlag:?3 OR gxeFlag:?4 &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndNotesAndSumstatsFlagOrGxeFlag
            (String efoTrait, String reportedTrait, String notes,
             boolean sumstatsFlag, boolean gxeFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND notes:\"*?2*\" AND pooledFlag:?3 OR gxeFlag:?4 &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndNotesAndPooledFlagOrGxeFlag
            (String efoTrait, String reportedTrait, String notes,
             boolean pooledFlag, boolean gxeFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND notes:\"*?2*\" AND sumstatsFlag:?3 OR pooledFlag:?4 &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndNotesAndSumstatsFlagOrPooledFlag
            (String efoTrait, String reportedTrait, String notes,
             boolean sumstatsFlag, boolean pooledFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND notes:\"*?2*\" AND sumstatsFlag:?3 &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndNotesAndSumstatsFlag
            (String efoTrait, String reportedTrait, String notes,
             boolean sumstatsFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND notes:\"*?2*\" AND pooledFlag:?3 &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndNotesAndPooledFlag
            (String efoTrait, String reportedTrait, String notes,
             boolean pooledFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND sumstatsFlag:?2 OR gxeFlag:?3 OR pooledFlag:?4 &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndSumstatsFlagOrGxeFlagOrPooledFlag
            (String efoTrait, String reportedTrait, boolean sumstatsFlag,
             boolean gxeFlag, boolean pooledFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND sumstatsFlag:?2 OR gxeFlag:?3 &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndSumstatsFlagOrGxeFlag
            (String efoTrait, String reportedTrait, boolean sumstatsFlag,
             boolean gxeFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND pooledFlag:?2 OR gxeFlag:?3 &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndPooledFlagOrGxeFlag
            (String efoTrait, String reportedTrait, boolean pooledFlag,
             boolean gxeFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND pooledFlag:?2 OR sumstatsFlag:?3 &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndPooledFlagOrSumstatsFlag
            (String efoTrait, String reportedTrait, boolean pooledFlag,
             boolean sumstatsFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND sumstatsFlag:?2 &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndSumstatsFlag
            (String efoTrait, String reportedTrait,
             boolean sumstatsFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND pooledFlag:?2 &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndPooledFlag
            (String efoTrait, String reportedTrait,
             boolean pooledFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND gxeFlag:?2 &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndGxeFlag
            (String efoTrait, String reportedTrait,
             boolean gxeFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND sumstatsFlag:?1 OR gxeFlag:?2 OR pooledFlag:?3 &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndSumstatsFlagOrGxeFlagOrPooledFlag
            (String efoTrait, boolean sumstatsFlag,
             boolean gxeFlag, boolean pooledFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND sumstatsFlag:?1 OR gxeFlag:?2 &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndSumstatsFlagOrGxeFlag
            (String efoTrait, boolean sumstatsFlag,
             boolean gxeFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND pooledFlag:?1 OR gxeFlag:?2 &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndPooledFlagOrGxeFlag
            (String efoTrait, boolean pooledFlag,
             boolean gxeFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND pooledFlag:?1 OR sumstatsFlag:?2 &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndPooledFlagOrSumstatsFlag
            (String efoTrait, boolean pooledFlag,
             boolean sumstatsFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND pooledFlag:?1 &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndPooledFlag
            (String efoTrait, boolean pooledFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND sumstatsFlag:?1 &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndSumstatsFlag
            (String efoTrait, boolean sumstatsFlag, Pageable page);


    @Query("efoTraits:\"*?0*\" AND sumstatsFlag:?1 &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndGxeFlag
            (String efoTrait, boolean gxeFlag, Pageable page);

    @Query("reportedTrait:\"*?0*\" AND sumstatsFlag:?1 OR gxeFlag:?2 OR pooledFlag:?3 &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndSumstatsFlagOrGxeFlagOrPooledFlag
            (String reportedTrait, boolean sumstatsFlag,
             boolean gxeFlag, boolean pooledFlag, Pageable page);

    @Query("reportedTrait:\"*?0*\" AND sumstatsFlag:?1 OR gxeFlag:?2 &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndSumstatsFlagOrGxeFlag
            (String reportedTrait, boolean sumstatsFlag,
             boolean gxeFlag, Pageable page);

    @Query("reportedTrait:\"*?0*\" AND pooledFlag:?1 OR gxeFlag:?2 &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndPooledFlagOrGxeFlag
            (String reportedTrait, boolean pooledFlag,
             boolean gxeFlag, Pageable page);

    @Query("reportedTrait:\"*?0*\" AND pooledFlag:?1 OR sumstatsFlag:?2 &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndPooledFlagOrSumstatsFlag
            (String reportedTrait, boolean pooledFlag,
             boolean sumstatsFlag, Pageable page);

    @Query("reportedTrait:\"*?0*\" AND pooledFlag:?1 &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndPooledFlag
            (String reportedTrait, boolean pooledFlag, Pageable page);

    @Query("reportedTrait:\"*?0*\" AND sumstatsFlag:?1 &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndSumstatsFlag
            (String reportedTrait, boolean sumstatsFlag, Pageable page);

    @Query("reportedTrait:\"*?0*\" AND gxeFlag:?1 &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndGxeFlag
            (String reportedTrait, boolean gxeFlag, Pageable page);


    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND notes:\"*?2*\" AND gxeFlag:?3 &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndNotesAndGxeFlag
            (String efoTrait, String reportedTrait, String notes,
             boolean gxeFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" AND notes:\"*?2*\" &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTraitAndNotes
            (String efoTrait, String reportedTrait, String notes, Pageable page);

    @Query("efoTraits:\"*?0*\" AND notes:\"*?1*\" AND sumstatsFlag:?2 OR gxeFlag:?3 OR pooledFlag:?4 &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndNotesAndSumstatsFlagOrGxeFlagOrPooledFlag
            (String efoTrait, String notes, boolean sumstatsFlag,
             boolean gxeFlag, boolean pooledFlag, Pageable page);


    @Query("efoTraits:\"*?0*\" AND notes:\"*?1*\" AND sumstatsFlag:?2 OR gxeFlag:?3 &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndNotesAndSumstatsFlagOrGxeFlag
            (String efoTrait, String notes, boolean sumstatsFlag,
             boolean gxeFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND notes:\"*?1*\" AND sumstatsFlag:?2 OR pooledFlag:?3 &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndNotesAndSumstatsFlagOrPooledFlag
            (String efoTrait, String notes, boolean sumstatsFlag,
             boolean pooledFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND notes:\"*?1*\" AND pooledFlag:?2 OR gxeFlag:?3 &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndNotesAndPooledFlagOrGxeFlag
            (String efoTrait, String notes, boolean pooledFlag,
             boolean gxeFlag, Pageable page);

    @Query("efoTraits:\"*?0*\" AND notes:\"*?1*\" AND pooledFlag:?2 &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndNotesAndPooledFlag
            (String efoTrait, String notes, boolean pooledFlag
                    , Pageable page);

    @Query("efoTraits:\"*?0*\" AND notes:\"*?1*\" AND gxeFlag:?2 &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndNotesAndGxeFlag
            (String efoTrait, String notes, boolean gxeFlag
                    , Pageable page);

    @Query("efoTraits:\"*?0*\" AND notes:\"*?1*\" AND sumstatsFlag:?2 &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndNotesAndSumstatsFlag
            (String efoTrait, String notes, boolean sumstatsFlag
                    , Pageable page);

    @Query("efoTraits:\"*?0*\" AND notes:\"*?1*\"&defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsAndNotes
            (String efoTrait, String notes
                    , Pageable page);


    @Query("reportedTrait:\"*?0*\" AND notes:\"*?1*\" AND sumstatsFlag:?2 OR gxeFlag:?3 OR pooledFlag:?4 &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndNotesAndSumstatsFlagOrGxeFlagOrPooledFlag
            (String reportedTrait, String notes, boolean sumstatsFlag,
             boolean gxeFlag, boolean pooledFlag, Pageable page);


    @Query("reportedTrait:\"*?0*\" AND notes:\"*?1*\" AND sumstatsFlag:?2 OR gxeFlag:?3 &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndNotesAndSumstatsFlagOrGxeFlag
            (String reportedTrait, String notes, boolean sumstatsFlag,
             boolean gxeFlag, Pageable page);

    @Query("reportedTrait:\"*?0*\" AND notes:\"*?1*\" AND sumstatsFlag:?2 OR pooledFlag:?3 &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndNotesAndSumstatsFlagOrPooledFlag
            (String reportedTrait, String notes, boolean sumstatsFlag,
             boolean pooledFlag, Pageable page);

    @Query("reportedTrait:\"*?0*\" AND notes:\"*?1*\" AND pooledFlag:?2 OR gxeFlag:?3 &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndNotesAndPooledFlagOrGxeFlag
            (String reportedTrait, String notes, boolean pooledFlag,
             boolean gxeFlag, Pageable page);

    @Query("reportedTrait:\"*?0*\" AND notes:\"*?1*\" AND pooledFlag:?2 &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndNotesAndPooledFlag
            (String reportedTrait, String notes, boolean pooledFlag
                    , Pageable page);

    @Query("reportedTrait:\"*?0*\" AND notes:\"*?1*\" AND gxeFlag:?2 &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndNotesAndGxeFlag
            (String reportedTrait, String notes, boolean gxeFlag
                    , Pageable page);

    @Query("reportedTrait:\"*?0*\" AND notes:\"*?1*\" AND sumstatsFlag:?2 &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndNotesAndSumstatsFlag
            (String reportedTrait, String notes, boolean sumstatsFlag
                    , Pageable page);


    @Query("reportedTrait:\"*?0*\" AND notes:\"*?1*\" &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitAndNotes
            (String efoTrait, String notes
                    , Pageable page);

    @Query("notes:\"*?0*\" AND sumstatsFlag:?1 OR gxeFlag:?2 OR pooledFlag:?3")
    Page<StudySolr> findByNotesAndSumstatsFlagOrGxeFlagOrPooledFlag
            (String notes, boolean sumstatsFlag,
             boolean gxeFlag, boolean pooledFlag, Pageable page);


    @Query("notes:\"*?0*\" AND sumstatsFlag:?1 OR gxeFlag:?2")
    Page<StudySolr> findByNotesAndSumstatsFlagOrGxeFlag
            (String notes, boolean sumstatsFlag,
             boolean gxeFlag, Pageable page);

    @Query("notes:\"*?0*\" AND sumstatsFlag:?1 OR pooledFlag:?2")
    Page<StudySolr> findByNotesAndSumstatsFlagOrPooledFlag
            (String notes, boolean sumstatsFlag,
             boolean pooledFlag, Pageable page);

    @Query("notes:\"*?0*\" AND pooledFlag:?1 OR gxeFlag:?2")
    Page<StudySolr> findByNotesAndPooledFlagOrGxeFlag
            (String notes, boolean pooledFlag,
             boolean gxeFlag, Pageable page);

    @Query("notes:\"*?0*\" AND pooledFlag:?1")
    Page<StudySolr> findByNotesAndPooledFlag
            (String notes, boolean pooledFlag
                    , Pageable page);

    @Query("notes:\"*?0*\" AND gxeFlag:?1")
    Page<StudySolr> findByNotesAndGxeFlag
            (String notes, boolean gxeFlag
                    , Pageable page);

    @Query("notes:\"*?0*\" AND sumstatsFlag:?1")
    Page<StudySolr> findByNotesAndSumstatsFlag
            (String notes, boolean sumstatsFlag
                    , Pageable page);


    @Query("efoTraits:\"*?0*\" AND reportedTrait:\"*?1*\" &defType=edismax&qf=reportedTrait^2.0 efoTraits^4.0")
    Page<StudySolr> findByEfoTraitsAndReportedTrait
            (String efoTrait, String reportedTrait, Pageable page);


    @Query("efoTraits:\"*?0*\" &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraits(String efoTrait, Pageable page);

    @Query("efoTraits:*?0* &defType=edismax&qf=efoTraits^2.0")
    Page<StudySolr> findByEfoTraitsWithoutSpaces(String efoTrait, Pageable page);

    //@Query("reportedTrait:?0&defType=edismax&qf=reportedTrait^2.0")
    @Query("reportedTrait:\"*?0*\" &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTrait(String reportedTrait, Pageable page);

    @Query("reportedTrait:*?0* &defType=edismax&qf=reportedTrait^2.0")
    Page<StudySolr> findByReportedTraitWithoutSpaces(String reportedTrait, Pageable page);

    @Query(value = "notes:\"*?0*\"")
    Page<StudySolr> findByNotes(String notes, Pageable page);

    @Query(value = "notes:*?0*")
    Page<StudySolr> findByNotesWithoutSpaces(String notes, Pageable page);

    @Query(value = "sumstatsFlag:?0 OR pooledFlag:?1 OR gxeFlag:?2")
    Page<StudySolr> findBySumstatsFlagOrPooledFlagOrGxeFlag(boolean sumstatsFlag, boolean pooledFlag, boolean gxeFlag, Pageable page);

    @Query(value = "sumstatsFlag:?0 OR pooledFlag:?1")
    Page<StudySolr> findBySumstatsFlagOrPooledFlag(boolean sumstatsFlag, boolean pooledFlag, Pageable page);

    @Query(value = "pooledFlag:?0 OR gxeFlag:?1")
    Page<StudySolr> findByPooledFlagOrGxeFlag(boolean pooledFlag, boolean gxeFlag, Pageable page);

    @Query(value = "sumstatsFlag:?0 OR gxeFlag:?1")
    Page<StudySolr> findBySumstatsFlagOrGxeFlag(boolean sumstatsFlag, boolean gxeFlag, Pageable page);

    @Query(value = "sumstatsFlag:?0")
    Page<StudySolr> findBySumstatsFlag(boolean sumstatsFlag, Pageable page);

    @Query(value = "pooledFlag:?0")
    Page<StudySolr> findByPooledFlag(boolean pooledFlag, Pageable page);

    @Query(value = "gxeFlag:?0")
    Page<StudySolr> findByGxeFlag(boolean gxeFlag, Pageable page);

    @Query(value = "pmId:?0")
    Page<StudySolr> findByPmid(String pmId, Pageable page);

    @Query(value = "submissionId:?0")
    Page<StudySolr> findBySubmissionId(String submissionId, Pageable page);

    @Query(value = "submissionId:?0 AND accessionId:?1")
    Optional<StudySolr> findBySubmissionIdAndAccessionId(String submissionId, String accessionId);

    @Query(value = "bowId:?0")
    Page<StudySolr> findByBowId(String bowId, Pageable page);

    @Query(value = "accessionId:?0")
    Page<StudySolr>  findByAccessionId(String accessionId, Pageable page);

}

