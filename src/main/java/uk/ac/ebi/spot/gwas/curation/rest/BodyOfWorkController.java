package uk.ac.ebi.spot.gwas.curation.rest;

import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.dto.BodyOfWorkDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.BodyOfWorkService;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.dto.BodyOfWorkDto;

@RestController()
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_BODY_OF_WORK)
public class BodyOfWorkController {

    final BodyOfWorkService bodyOfWorkService;
    final BodyOfWorkDtoAssembler bodyOfWorkDtoAssembler;

    public BodyOfWorkController(BodyOfWorkService bodyOfWorkService, BodyOfWorkDtoAssembler bodyOfWorkDtoAssembler) {
        this.bodyOfWorkService = bodyOfWorkService;
        this.bodyOfWorkDtoAssembler = bodyOfWorkDtoAssembler;
    }

    @GetMapping(value = "/{id}")
    public Resource<BodyOfWorkDto> getBodyOfWork(@PathVariable String id) {
        return bodyOfWorkDtoAssembler.toResource(bodyOfWorkService.findById(id));
    }

}
