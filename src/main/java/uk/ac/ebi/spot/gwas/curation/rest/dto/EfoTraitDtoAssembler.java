package uk.ac.ebi.spot.gwas.curation.rest.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.EfoTraitController;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EFOTraitWrapperDTO;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EfoTraitDto;
import uk.ac.ebi.spot.gwas.deposition.exception.FileProcessingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
public class EfoTraitDtoAssembler implements ResourceAssembler<EfoTrait, Resource<EfoTraitDto>> {

    private static final Logger log = LoggerFactory.getLogger(EfoTraitDtoAssembler.class);

    private final UserService userService;


    private final DepositionCurationConfig depositionCurationConfig;

    public EfoTraitDtoAssembler(UserService userService, DepositionCurationConfig depositionCurationConfig) {
        this.userService = userService;
        this.depositionCurationConfig = depositionCurationConfig;
    }

    @Override
    public Resource<EfoTraitDto> toResource(EfoTrait efoTrait) {
        EfoTraitDto efoTraitDto = EfoTraitDto.builder()
                .efoTraitId(efoTrait.getId())
                .trait(efoTrait.getTrait())
                .uri(efoTrait.getUri())
                .shortForm(efoTrait.getShortForm())
                .created(efoTrait.getCreated() != null ? ProvenanceDtoAssembler.assemble(efoTrait.getCreated(),
                        userService.getUser(efoTrait.getCreated().getUserId())) : null)
                .updated(efoTrait.getUpdated() != null ? ProvenanceDtoAssembler.assemble(efoTrait.getUpdated(),
                        userService.getUser(efoTrait.getUpdated().getUserId())) : null)
                .build();
        log.info("EfoTraitDtoAssembler Builder ->" + efoTraitDto);
        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(EfoTraitController.class).getEfoTrait(efoTrait.getId()));
        Resource<EfoTraitDto> resource = new Resource<>(efoTraitDto);
        //resource.add(controllerLinkBuilder.withSelfRel());
        resource.add(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).withRel(DepositionCurationConstants.LINKS_PARENT));
        log.info("EfoTraitDtoAssembler Resource ->" + resource);
        return resource;
    }

    public static List<EfoTraitDto> assemble(List<EfoTrait> efoTraits) {

        List<EfoTraitDto> efoTraitDtos = new ArrayList<>();
        if(efoTraits != null && !efoTraits.isEmpty())
            efoTraits.forEach(efoTrait -> {
                EfoTraitDto efoTraitDto = EfoTraitDto.builder()
                        .efoTraitId(efoTrait.getId())
                        .trait(efoTrait.getTrait())
                        .uri(efoTrait.getUri())
                        .shortForm(efoTrait.getShortForm())
                        .build();
                efoTraitDtos.add(efoTraitDto);
            });
        return efoTraitDtos;
    }


    public EfoTraitDto assemble(EfoTrait efoTrait) {

        EfoTraitDto efoTraitDto = EfoTraitDto.builder()
                .efoTraitId(efoTrait.getId())
                .trait(efoTrait.getTrait())
                .uri(efoTrait.getUri())
                .shortForm(efoTrait.getShortForm())
                .created(efoTrait.getCreated() != null ? ProvenanceDtoAssembler.assemble(efoTrait.getCreated(),
                        userService.getUser(efoTrait.getCreated().getUserId())) : null)
                .updated(efoTrait.getUpdated() != null ? ProvenanceDtoAssembler.assemble(efoTrait.getUpdated(),
                        userService.getUser(efoTrait.getUpdated().getUserId())) : null)
                .build();


        return efoTraitDto;
    }

    public EfoTrait disassemble(EfoTraitDto efoTraitDto) {
        EfoTrait efoTrait = new EfoTrait();
        Optional.ofNullable(efoTraitDto.getEfoTraitId()).ifPresent(id -> efoTrait.setId(efoTraitDto.getEfoTraitId()));
        Optional.ofNullable(efoTraitDto.getTrait()).ifPresent(trait -> efoTrait.setTrait(efoTraitDto.getTrait().trim()));
        Optional.ofNullable(efoTraitDto.getUri()).ifPresent(uri -> efoTrait.setUri(efoTraitDto.getUri().trim()));
        Optional.ofNullable(efoTraitDto.getShortForm()).ifPresent(studies -> efoTrait.setShortForm(efoTraitDto.getShortForm().trim()));
        return efoTrait;
    }

    public List<EfoTrait> disassemble(MultipartFile multipartFile) {

        CsvMapper csvMapper = new CsvMapper();
        CsvSchema csvSchema = FileHandler.getSchemaFromMultiPartFile(multipartFile);
        List<EfoTraitDto> efoTraitDtos;

        try {
            MappingIterator<EfoTraitDto> iterator = csvMapper.readerFor(EfoTraitDto.class).with(csvSchema).readValues(multipartFile.getInputStream());
            efoTraitDtos = iterator.readAll();
        } catch (IOException e) {
            log.error("Exception in EFOTrait disassemble "+e.getMessage(),e);
            throw new FileProcessingException("Could not read the file "+e.getMessage());
        }
        List<EfoTrait> efoTraits = new ArrayList<>();
        efoTraitDtos.forEach(efoTraitDto -> efoTraits.add(disassemble(efoTraitDto)));
        return efoTraits;
    }



}
