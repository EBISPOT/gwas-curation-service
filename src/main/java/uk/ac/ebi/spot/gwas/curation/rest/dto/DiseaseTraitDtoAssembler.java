package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.rest.DiseaseTraitController;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;

import java.util.ArrayList;
import java.util.List;

@Component
public class DiseaseTraitDtoAssembler implements ResourceAssembler<DiseaseTrait, Resource<DiseaseTraitDto>> {

    @Autowired
    UserService userService;

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    public Resource<DiseaseTraitDto> toResource(DiseaseTrait diseaseTrait) {
        DiseaseTraitDto diseaseTraitDTO = DiseaseTraitDto.builder()
                .diseaseTraitId(diseaseTrait.getId())
                .trait(diseaseTrait.getTrait())
                .studies(diseaseTrait.getStudyIds())
                .created(ProvenanceDtoAssembler.assemble(diseaseTrait.getCreated(),
                        userService.getUser(diseaseTrait.getCreated().getUserId())))
                .build();
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(DiseaseTraitController.class).getDiseaseTrait(diseaseTrait.getId()));
        Resource<DiseaseTraitDto> resource = new Resource<>(diseaseTraitDTO);
        //resource.add(controllerLinkBuilder.withSelfRel());
        resource.add(new Link(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).toUri().toString()));
        return resource;
    }

    public static List<DiseaseTraitDto> assemble(List<DiseaseTrait> diseaseTraits) {

        List<DiseaseTraitDto> diseaseTraitDTOS = new ArrayList<>();
        diseaseTraits.forEach(diseaseTrait -> {
            DiseaseTraitDto diseaseTraitDTO = DiseaseTraitDto.builder()
                    .diseaseTraitId(diseaseTrait.getId())
                    .trait(diseaseTrait.getTrait())
                    .build();
            diseaseTraitDTOS.add(diseaseTraitDTO);
        });
        return diseaseTraitDTOS;
    }

    public static DiseaseTrait disassemble(DiseaseTraitDto diseaseTraitDTO) {
        DiseaseTrait diseaseTrait = new DiseaseTrait();
        diseaseTrait.setTrait(diseaseTraitDTO.getTrait());
        return diseaseTrait;
    }


}







