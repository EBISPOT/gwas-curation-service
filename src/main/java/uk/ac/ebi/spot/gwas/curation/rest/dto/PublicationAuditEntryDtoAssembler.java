package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.rest.PublicationAuditEntryController;
import uk.ac.ebi.spot.gwas.curation.service.PublicationAuditEntryService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.deposition.audit.PublicationAuditEntryDto;

import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuditEntry;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Component
public class PublicationAuditEntryDtoAssembler extends ResourceSupport implements ResourceAssembler<PublicationAuditEntry, Resource<PublicationAuditEntryDto>> {

    @Autowired
    UserService userService;

    @Autowired
    UserDtoAssembler userDtoAssembler;

    @Autowired
    PublicationAuditEntryService publicationAuditEntryService;
    @Override
    public Resource<PublicationAuditEntryDto> toResource(PublicationAuditEntry auditEntry) {
        PublicationAuditEntryDto publicationAuditEntryDto = PublicationAuditEntryDto.builder()
                .publicationId(auditEntry.getPublicationId())
                .event(auditEntry.getEvent())
                .eventDetails(auditEntry.getEventDetails())
                .provenanceDto(ProvenanceDtoAssembler.assemble(new Provenance(auditEntry.getTimestamp(),
                                userService.findUserDetailsUsingEmail(auditEntry.getUserId()).getId()),
                        userService.findUserDetailsUsingEmail(auditEntry.getUserId())
                        ))
                .build();

        Resource<PublicationAuditEntryDto> resource = new Resource<>(publicationAuditEntryDto);
        resource.add(linkTo(methodOn(PublicationAuditEntryController.class).getAuditEntry(auditEntry.getId())).withSelfRel());
        return resource;
    }



}
