package uk.ac.ebi.spot.gwas.curation.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.FileUploadRequest;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.TraitUploadReport;
import uk.ac.ebi.spot.gwas.deposition.exception.FileValidationException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_DISEASE_TRAITS_FILE_UPLOAD)
public class DiseaseTraitFileUploadController {

    @Autowired
    DiseaseTraitService diseaseTraitService;

    @Autowired
    UserService userService;

    @Autowired
    JWTService jwtService;

    @Autowired
    DiseaseTraitDtoAssembler diseaseTraitDtoAssembler;


    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TraitUploadReport>> uploadDiseaseTraits(@Valid FileUploadRequest fileUploadRequest, BindingResult result,
                                                                        HttpServletRequest request) {
        if (result.hasErrors()) {
            throw new FileValidationException(result);
        }
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        MultipartFile multipartFile = fileUploadRequest.getMultipartFile();
        List<DiseaseTrait> diseaseTraits = diseaseTraitDtoAssembler.disassemble(multipartFile);
        List<TraitUploadReport> traitUploadReports = diseaseTraitService.createDiseaseTrait(diseaseTraits, user);
        return new ResponseEntity<>(traitUploadReports, HttpStatus.CREATED);
    }

}
