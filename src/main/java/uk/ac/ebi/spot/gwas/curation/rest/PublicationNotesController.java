package uk.ac.ebi.spot.gwas.curation.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.dto.PublicationNotesDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.JWTService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationNotesService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.GeneralCommon;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationNotes;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationNotesDto;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = GeneralCommon.API_V1 + DepositionCurationConstants.API_PUBLICATIONS)
public class PublicationNotesController {
    @Autowired
    UserService userService;
    @Autowired
    JWTService jwtService;

    @Autowired
    PublicationNotesService publicationNotesService;



    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @Autowired
    PublicationNotesDtoAssembler publicationNotesDtoAssembler;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{pubId}/notes")
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public Resource<PublicationNotesDto> getPublicationNotes(@PathVariable String pubId) {

        return publicationNotesDtoAssembler.toResource(publicationNotesService.getNotes(pubId));
    }


    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/{pubId}/notes")
    @PreAuthorize("hasRole('self.GWAS_Curator')")
    public Resource<PublicationNotesDto> createPublicationNotes(@PathVariable String pubId,
                                                                @Valid  @RequestBody  PublicationNotesDto publicationNotesDto,
                                                                HttpServletRequest request) {
        User user = userService.findUser(jwtService.extractUser(CurationUtil.parseJwt(request)), false);

        return publicationNotesDtoAssembler.toResource(publicationNotesService.createNotes(publicationNotesDto, user, pubId));
    }

}
