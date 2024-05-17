package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.domain.LiteratureFile;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.LiteratureFileDto;

@Component
public class LiteratureFileAssembler implements ResourceAssembler<LiteratureFile, Resource<LiteratureFileDto>> {

    @Override
    public Resource<LiteratureFileDto> toResource(LiteratureFile literatureFile) {

        LiteratureFileDto literatureFileDto = LiteratureFileDto.builder()
                .id(literatureFile.getId())
                .fileName(literatureFile.getName())
                .createdBy(literatureFile.getCreatedBy())
                .createDate(literatureFile.getCreateDate()).build();
        return new Resource<>(literatureFileDto);
    }

}
