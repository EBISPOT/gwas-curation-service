package uk.ac.ebi.spot.gwas.curation.service;

import uk.ac.ebi.spot.gwas.deposition.domain.Association;
import uk.ac.ebi.spot.gwas.deposition.dto.AssociationDto;
import uk.ac.ebi.spot.gwas.deposition.javers.AddedRemoved;
import uk.ac.ebi.spot.gwas.deposition.javers.ElementChange;
import uk.ac.ebi.spot.gwas.deposition.javers.ValueChangeWrapper;
import uk.ac.ebi.spot.gwas.deposition.javers.VersionDiffStats;

import java.util.List;

public interface AssociationJaversService {

    public VersionDiffStats findAssociationChanges(String tag, List<Association> prevAscns, List<Association> newAscns, VersionDiffStats diffStats);

    public List<ValueChangeWrapper> diffAssociations(AssociationDto dto1, AssociationDto dto2);

    public AddedRemoved getAssociationVersionStats(List<Association> prevAscns, List<Association> newAscns);

    public Association processAssociationTag(ElementChange elementChange);
}
