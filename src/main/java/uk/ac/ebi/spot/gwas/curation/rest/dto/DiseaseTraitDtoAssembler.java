package uk.ac.ebi.spot.gwas.curation.rest.dto;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.DiseaseTraitController;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;
import uk.ac.ebi.spot.gwas.deposition.exception.FileProcessingException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class DiseaseTraitDtoAssembler implements ResourceAssembler<DiseaseTrait, Resource<DiseaseTraitDto>> {

    private static final Logger log = LoggerFactory.getLogger(DiseaseTraitDtoAssembler.class);
    @Autowired
    UserService userService;

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @Override
    public Resource<DiseaseTraitDto> toResource(DiseaseTrait diseaseTrait) {
        DiseaseTraitDto diseaseTraitDTO = DiseaseTraitDto.builder()
                .diseaseTraitId(diseaseTrait.getId())
                .trait(diseaseTrait.getTrait())
                .created(diseaseTrait.getCreated() != null ? ProvenanceDtoAssembler.assemble(diseaseTrait.getCreated(),
                        userService.getUser(diseaseTrait.getCreated().getUserId())) : null)
                .updated(diseaseTrait.getUpdated() != null ? ProvenanceDtoAssembler.assemble(diseaseTrait.getUpdated(),
                        userService.getUser(diseaseTrait.getUpdated().getUserId())) : null)
                .build();
        log.info("DiseaseTraitDtoAssembler Buider->"+diseaseTraitDTO);
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(DiseaseTraitController.class).getDiseaseTrait(diseaseTrait.getId()));
        Resource<DiseaseTraitDto> resource = new Resource<>(diseaseTraitDTO);
        //resource.add(controllerLinkBuilder.withSelfRel());
        resource.add(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).withRel(DepositionCurationConstants.LINKS_PARENT));

        log.info("DiseaseTraitDtoAssembler Resource->"+resource);
        return resource;
    }

    public static List<DiseaseTraitDto> assemble(List<DiseaseTrait> diseaseTraits) {

        List<DiseaseTraitDto> diseaseTraitDTOS = new ArrayList<>();
        if(diseaseTraits != null && !diseaseTraits.isEmpty())
        diseaseTraits.forEach(diseaseTrait -> {
            DiseaseTraitDto diseaseTraitDTO = DiseaseTraitDto.builder()
                    .diseaseTraitId(diseaseTrait.getId())
                    .trait(diseaseTrait.getTrait())
                    .build();
            diseaseTraitDTOS.add(diseaseTraitDTO);
        });
        return diseaseTraitDTOS;
    }


    public  DiseaseTraitDto assemble(DiseaseTrait diseaseTrait) {

            DiseaseTraitDto diseaseTraitDTO = DiseaseTraitDto.builder()
                    .diseaseTraitId(diseaseTrait.getId())
                    .trait(diseaseTrait.getTrait())
                    .created(diseaseTrait.getCreated() != null ? ProvenanceDtoAssembler.assemble(diseaseTrait.getCreated(),
                            userService.getUser(diseaseTrait.getCreated().getUserId())) : null)
                    .updated(diseaseTrait.getUpdated() != null ? ProvenanceDtoAssembler.assemble(diseaseTrait.getUpdated(),
                            userService.getUser(diseaseTrait.getUpdated().getUserId())) : null)
                    .build();


        return diseaseTraitDTO;
    }

    public  DiseaseTrait disassemble(DiseaseTraitDto diseaseTraitDTO) {
        DiseaseTrait diseaseTrait = new DiseaseTrait();
        Optional.ofNullable(diseaseTraitDTO.getDiseaseTraitId()).ifPresent(id -> diseaseTrait.setId(diseaseTraitDTO.getDiseaseTraitId()));
        Optional.ofNullable(diseaseTraitDTO.getTrait()).ifPresent(trait -> diseaseTrait.setTrait(diseaseTraitDTO.getTrait()));
        return diseaseTrait;
    }

    public  List<DiseaseTrait> disassemble(MultipartFile multipartFile)  {
        CsvMapper mapper = new CsvMapper();
         CsvSchema csvSchema = FileHandler.getSchemaFromMultiPartFile(multipartFile);
        List<DiseaseTraitDto> diseaseTraitDtos;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            MappingIterator<DiseaseTraitDto> iterator = mapper.readerFor(DiseaseTraitDto.class).with(csvSchema).readValues(inputStream);
            diseaseTraitDtos = iterator.readAll();
        }catch (IOException ex){
            throw new FileProcessingException("Could not read the file");
        }

        List<DiseaseTrait> diseaseTraits = new ArrayList<>();
        diseaseTraitDtos.forEach(diseaseTraitDTO -> {
            DiseaseTrait diseaseTrait = new DiseaseTrait();
            diseaseTrait.setTrait(diseaseTraitDTO.getTrait());
            diseaseTraits.add(diseaseTrait);
        });
        return diseaseTraits;
    }


}







