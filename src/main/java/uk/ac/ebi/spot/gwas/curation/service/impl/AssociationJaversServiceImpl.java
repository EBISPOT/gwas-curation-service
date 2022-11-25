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
import uk.ac.ebi.spot.gwas.curation.rest.dto.AssociationDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.AssociationJaversService;
import uk.ac.ebi.spot.gwas.curation.service.AssociationsService;
import uk.ac.ebi.spot.gwas.curation.service.JaversCommonService;
import uk.ac.ebi.spot.gwas.deposition.domain.Association;
import uk.ac.ebi.spot.gwas.deposition.dto.AssociationDto;
import uk.ac.ebi.spot.gwas.deposition.javers.AddedRemoved;
import uk.ac.ebi.spot.gwas.deposition.javers.ElementChange;
import uk.ac.ebi.spot.gwas.deposition.javers.ValueChangeWrapper;
import uk.ac.ebi.spot.gwas.deposition.javers.VersionDiffStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssociationJaversServiceImpl implements AssociationJaversService {

    private static final Logger log = LoggerFactory.getLogger(AssociationJaversServiceImpl.class);

    @Autowired
    JaversCommonService javersCommonService;

    @Autowired
    AssociationsService associationsService;

    public VersionDiffStats findAssociationChanges(String tag, List<Association> prevAscns, List<Association> newAscns, VersionDiffStats diffStats) {

        if(!newAscns.isEmpty())
            diffStats.setAssociations(new ArrayList<>());

        prevAscns.forEach((asscn) -> {
            //log.info("VariantId*****"+asscn.getVariantId());
            List<AssociationDto> newAsscnsDto = newAscns.stream()
                    .filter((ascn) -> ascn.getVariantId().equals(asscn.getVariantId()))
                    .map(AssociationDtoAssembler::assemble)
                    .collect(Collectors.toList());
            AssociationDto prevAsscnsDto = AssociationDtoAssembler.assemble(asscn);
            if (!newAsscnsDto.isEmpty()) {
                List<ValueChangeWrapper> valChanges = diffAssociations(prevAsscnsDto, newAsscnsDto.get(0));
                if(!valChanges.isEmpty()) {
                    VersionDiffStats versionDiffStats = new VersionDiffStats();
                    versionDiffStats.setEntity(asscn.getVariantId());
                    versionDiffStats.setEdited(valChanges.stream().
                            map(javersCommonService::mapChangetoVersionStats)
                            .collect(Collectors.toList()));
                    diffStats.getAssociations().add(versionDiffStats);
                }
            }

        });

        return diffStats;


    }

    public List<ValueChangeWrapper> diffAssociations(AssociationDto dto1, AssociationDto dto2) {
        Javers javers = JaversBuilder.javers().build();
        Diff diff = javers.compare(dto1, dto2);
        //log.info("************");
        //log.info("Diff Asscn"+ diff);
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

    public AddedRemoved getAssociationVersionStats(List<Association> prevAscns, List<Association> newAscns) {
        //log.info("Inside getAssociationVersionStats() ");

        List<String> newAscnsTags = newAscns.stream()
                .map(asscn -> asscn.getStudyTag() + asscn.getVariantId())
                .collect(Collectors.toList());

        List<String> prevAscnsTags = prevAscns.stream()
                .map(asscn -> asscn.getStudyTag() + asscn.getVariantId())
                .collect(Collectors.toList());

        List<Association> asscnsRemoved = prevAscns.stream()
                .filter(asscn -> !newAscnsTags.contains(asscn.getStudyTag() + asscn.getVariantId()))
                .collect(Collectors.toList());

        List<Association> asscnsAdded = newAscns.stream()
                .filter(asscn -> !prevAscnsTags.contains(asscn.getStudyTag() + asscn.getVariantId()))
                .collect(Collectors.toList());

        //log.info("newAscnsTags****"+newAscnsTags);
       // log.info("prevAscnsTags****"+prevAscnsTags);
       // log.info("asscnsRemoved****"+asscnsRemoved);
        //log.info("asscnsAdded****"+asscnsAdded);

        AddedRemoved addedRemoved = new AddedRemoved();
        addedRemoved.setAdded(asscnsAdded.size());
        addedRemoved.setRemoved(asscnsRemoved.size());

        return addedRemoved;
    }

    public String processAssociationTag(ElementChange elementChange){
        if (elementChange.getElementChangeType().equals("ValueAdded")){
            //return associationsService.getAssociation(elementChange.getValue().toString() );
            return elementChange.getValue().toString();
        }
        return null;
    }


}
