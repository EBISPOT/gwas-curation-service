package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.Study;
import uk.ac.ebi.spot.gwas.deposition.dto.StudyDto;
import uk.ac.ebi.spot.gwas.deposition.javers.AddedRemoved;
import uk.ac.ebi.spot.gwas.deposition.javers.ElementChange;
import uk.ac.ebi.spot.gwas.deposition.javers.ValueChangeWrapper;
import uk.ac.ebi.spot.gwas.deposition.javers.VersionDiffStats;

import java.util.List;

public interface StudyJaversService {

    public VersionDiffStats findStudyChanges(String tag, List<Study> studyList, List<Study> newStudies);

    public List<ValueChangeWrapper> diffStudies(StudyDto dto1, StudyDto dto2);

    public AddedRemoved getStudyVersionStats(List<Study> prevStudies, List<Study> newStudies, VersionDiffStats versionDiffStats);

    public AddedRemoved getReportedTraitVersionStats(List<Study> prevStudies, List<Study> newStudies);

    public AddedRemoved getReportedEfoVersionStats(List<Study> prevStudies, List<Study> newStudies);

    public String processStudyTag(ElementChange elementChange);

}
