package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.domain.LiteratureFile;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.LiteratureFileDto;

import java.util.ArrayList;
import java.util.List;

@Component
public class LiteratureFileAssembler implements ResourceAssembler<LiteratureFile, Resource<LiteratureFileDto>> {

    @Override
    public Resource<LiteratureFileDto> toResource(LiteratureFile literatureFile) {

        LiteratureFileDto literatureFileDto = LiteratureFileDto.builder()
                .id(literatureFile.getId())
                .originalFileName(literatureFile.getOriginalFileName())
                .onDiskFileName(literatureFile.getOnDiskFileName())
                .createDate(literatureFile.getCreated().getTimestamp().toDate())
                .createdBy(literatureFile.getCreated().getUserId())
                .build();
        return new Resource<>(literatureFileDto);
    }


    public static List<LiteratureFileDto> assemble(List<LiteratureFile> literatureFiles) {
        List<LiteratureFileDto> fileDtoList = new ArrayList<>();
        literatureFiles.forEach(literatureFile -> {
            fileDtoList.add(
                    LiteratureFileDto.builder()
                            .id(literatureFile.getId())
                            .originalFileName(literatureFile.getOriginalFileName())
                            .onDiskFileName(literatureFile.getOnDiskFileName())
                            .createDate(literatureFile.getCreated().getTimestamp().toDate())
                            .createdBy(literatureFile.getCreated().getUserId())
                            .build());
        });
        return fileDtoList;
    }


}
