package uk.ac.ebi.spot.gwas.curation.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uk.ac.ebi.spot.gwas.curation.rest.dto.LiteratureFileAssembler;
import uk.ac.ebi.spot.gwas.curation.service.impl.FtpServiceImpl;
import uk.ac.ebi.spot.gwas.curation.service.LiteratureFileService;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.LiteratureFile;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.LiteratureFileDto;
import uk.ac.ebi.spot.gwas.deposition.exception.FileValidationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_PUBLICATIONS)
public class LiteratureFileController {

    @Autowired
    private FtpServiceImpl ftpService;
    @Autowired
    private LiteratureFileService literatureFileService;
    @Autowired
    UserService userService;
    @Autowired
    JWTService jwtService;
    @Autowired
    private LiteratureFileAssembler literatureFileAssembler;

    @PreAuthorize("hasRole('self.GWAS_Curator')")
    @PostMapping("/{pubmedId}" + DepositionCurationConstants.API_LITERATURE_FILES)
    public ResponseEntity<LiteratureFileDto> createLiteratureFiles(@PathVariable("pubmedId") String pubmedId,
                                                                   @Valid LiteratureFileDto fileDto, BindingResult result,
                                                                   HttpServletRequest request) {
        if (result.hasErrors()) {
            throw new FileValidationException(result);
        }
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);
        List<LiteratureFile> files = literatureFileService.createLiteratureFile(fileDto, pubmedId, user);
        fileDto.setId(files.get(0).getId());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(files.get(0).getId()).toUri();
        return new ResponseEntity<>(fileDto, ResponseEntity.created(location).build().getHeaders(), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('self.GWAS_Curator')")
    @GetMapping(value="/{pubmedId}/" + DepositionCurationConstants.API_LITERATURE_FILES, produces = MediaType.APPLICATION_JSON_VALUE)
    public PagedResources<Resource<LiteratureFileDto>> getLiteratureFiles(PagedResourcesAssembler<LiteratureFile> assembler,
                                                                          @PathVariable("pubmedId") String pubmedId,
                                                                          @SortDefault Pageable pageable) {
        Page<LiteratureFile> files = literatureFileService.getLiteratureFiles(pageable, pubmedId);
        return assembler.toResource(files, literatureFileAssembler);
    }

    @PreAuthorize("hasRole('self.GWAS_Curator')")
    @GetMapping("/{pubmedId}/" + DepositionCurationConstants.API_LITERATURE_FILES + "/{fileId}")
    public ResponseEntity<InputStreamResource> downloadFiles(@PathVariable("pubmedId") String pubmedId,
                                                             @PathVariable("fileId") String fileId,
                                                             HttpServletResponse response) throws IOException {
        LiteratureFile file = literatureFileService.getLiteratureFile(fileId, pubmedId);
        InputStreamResource resource = ftpService.downloadFile(file.getName(), pubmedId);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM + ";charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        response.getOutputStream().flush();
        return ResponseEntity.ok().body(resource);
    }


}

