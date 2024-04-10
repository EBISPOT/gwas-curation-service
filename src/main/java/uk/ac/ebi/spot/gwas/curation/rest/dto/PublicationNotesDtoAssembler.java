package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationNotesRepository;
import uk.ac.ebi.spot.gwas.curation.rest.PublicationNotesController;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationNotes;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationNotesDto;
@Component
public class PublicationNotesDtoAssembler implements ResourceAssembler<PublicationNotes, Resource<PublicationNotesDto>> {


    @Autowired
    UserService userService;

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @Autowired
    PublicationNotesRepository publicationNotesRepository;

    @Override
    public Resource<PublicationNotesDto> toResource(PublicationNotes  publicationNotes) {
        PublicationNotesDto publicationNotesDto = PublicationNotesDto.builder()
                .publicationId(publicationNotes.getPublicationId())
                .created(publicationNotes.getCreated() != null ? ProvenanceDtoAssembler.assemble(publicationNotes.getCreated(),
                        userService.getUser(publicationNotes.getCreated().getUserId())): null)
                .updated(publicationNotes.getUpdated() != null ? ProvenanceDtoAssembler.assemble(publicationNotes.getUpdated(),
                        userService.getUser(publicationNotes.getCreated().getUserId())): null)
                .notes(publicationNotes.getNotes())
                .build();

        final ControllerLinkBuilder controllerLinkBuilder = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(PublicationNotesController.class).getPublicationNotes(publicationNotes.getPublicationId()));
        Resource<PublicationNotesDto> resource = new Resource<>(publicationNotesDto);
        resource.add(BackendUtil.underBasePath(controllerLinkBuilder, depositionCurationConfig.getProxy_prefix()).withRel(DepositionCurationConstants.LINKS_PARENT));
        return resource;
    }


    public PublicationNotesDto assemble(PublicationNotes  publicationNotes) {
        return PublicationNotesDto.builder()
                .publicationId(publicationNotes.getPublicationId())
                .created(publicationNotes.getCreated() != null ? ProvenanceDtoAssembler.assemble(publicationNotes.getCreated(),
                        userService.getUser(publicationNotes.getCreated().getUserId())): null)
                .updated(publicationNotes.getUpdated() != null ? ProvenanceDtoAssembler.assemble(publicationNotes.getUpdated(),
                        userService.getUser(publicationNotes.getCreated().getUserId())): null)
                .notes(publicationNotes.getNotes())
                .build();

    }

    public PublicationNotes disassemble(PublicationNotesDto  publicationNotesDto, User user) {
        PublicationNotes publicationNotes = new PublicationNotes();
        publicationNotes.setPublicationId(publicationNotesDto.getPublicationId());
        publicationNotes.setNotes(publicationNotesDto.getNotes());
        publicationNotes.setCreated(new Provenance(DateTime.now(), user.getId()));
        publicationNotes.setUpdated(new Provenance(DateTime.now(), user.getId()));
        return publicationNotes;
    }



}
