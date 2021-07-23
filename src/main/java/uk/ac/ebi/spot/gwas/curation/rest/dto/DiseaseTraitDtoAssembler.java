package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.rest.DiseaseTraitController;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;

import java.util.ArrayList;
import java.util.List;

@Component
public class DiseaseTraitDtoAssembler implements ResourceAssembler<DiseaseTrait, Resource<DiseaseTraitDto>> {

    public Resource<DiseaseTraitDto> toResource(DiseaseTrait diseaseTrait) {
        DiseaseTraitDto diseaseTraitDTO = DiseaseTraitDto.builder()
                .id(diseaseTrait.getId())
                .trait(diseaseTrait.getTrait())
                .studies(diseaseTrait.getStudyIds())
                .build();
        final ControllerLinkBuilder controllerLinkBuilder = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(DiseaseTraitController.class).getDiseaseTrait(diseaseTrait.getId()));
        Resource<DiseaseTraitDto> resource = new Resource<>(diseaseTraitDTO);
        resource.add(controllerLinkBuilder.withSelfRel());
        return resource;
    }

    public static List<DiseaseTraitDto> assemble(List<DiseaseTrait> diseaseTraits) {

        List<DiseaseTraitDto> diseaseTraitDTOS = new ArrayList<>();
        diseaseTraits.forEach(diseaseTrait -> {
            DiseaseTraitDto diseaseTraitDTO = DiseaseTraitDto.builder()
                    .id(diseaseTrait.getId())
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







