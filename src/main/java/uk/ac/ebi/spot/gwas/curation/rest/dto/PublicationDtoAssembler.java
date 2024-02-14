package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.rest.PublicationsController;
import uk.ac.ebi.spot.gwas.curation.service.CurationStatusService;
import uk.ac.ebi.spot.gwas.curation.service.CuratorService;

import uk.ac.ebi.spot.gwas.curation.service.PublicationAuthorService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.CorrespondingAuthorDto;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationDto;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PublicationDtoAssembler  implements ResourceAssembler<Publication, Resource<PublicationDto>> {

    ProvenanceDtoAssembler provenanceDtoAssembler;

    @Autowired
    PublicationAuthorService publicationAuthorService;

    @Autowired
    CurationStatusService curationStatusService;

    @Autowired
    CurationStatusDTOAssembler curationStatusDTOAssembler;

    @Autowired
    CuratorService curatorService;

    @Autowired
    CuratorDTOAssembler curatorDTOAssembler;

    @Autowired
    PublicationAuthorDtoAssembler publicationAuthorDtoAssembler;

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    public  PublicationDto assemble(Publication publication, User user) {

        return new PublicationDto(publication.getId(),
                publication.getPmid(),
                publication.getJournal(),
                publication.getTitle(),
                publication.getFirstAuthor(),
                publication.getPublicationDate(),
                publication.getCorrespondingAuthor() != null ?
                        new CorrespondingAuthorDto(publication.getCorrespondingAuthor().getAuthorName(),
                                publication.getCorrespondingAuthor().getEmail()) : null,
                publication.getStatus(),
                publication.getCreated() != null ? ProvenanceDtoAssembler.assemble(publication.getCreated(), user): null,
                publication.getUpdated() != null ? ProvenanceDtoAssembler.assemble(publication.getUpdated(), user): null,
                Optional.ofNullable(publication.getAuthors())
                        .filter(authors -> !authors.isEmpty())
                        .map(authors -> authors.stream().map(
                        authorId -> publicationAuthorService.getAuthorDetail(authorId)).
                        filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(author -> publicationAuthorDtoAssembler.assemble(author, user))
                        .collect(Collectors.toList()))
                        .orElse(null),
                publication.getCurationStatusId() != null ? curationStatusDTOAssembler.assemble(curationStatusService.findCurationStatus(publication.getCurationStatusId())) : null,
                publication.getCuratorId() != null ? curatorDTOAssembler.assemble(curatorService.findCuratorDetails(publication.getCuratorId())): null,
                publication.getSubmitter()
        );

    }


    public  Publication disassemble(PublicationDto publicationDto, User user) {
        return new Publication(publicationDto.getPmid(),
                publicationDto.getJournal(),
                publicationDto.getTitle(),
                publicationDto.getFirstAuthor(),
                publicationDto.getPublicationDate(),
                null,
                publicationDto.getStatus(),
                null,
                null,
                null,
                publicationDto.getCreated() != null ? provenanceDtoAssembler.disassemble(publicationDto.getCreated(), user) : null,
                publicationDto.getUpdated() != null ? provenanceDtoAssembler.disassemble(publicationDto.getUpdated(), user) : null,
                null);
    }

    @Override
    public Resource<PublicationDto> toResource(Publication publication) {
        PublicationDto publicationDto = PublicationDto
                .builder()
                .pmid(publication.getPmid())
                .publicationId(publication.getId())
                .title(publication.getTitle())
                .journal(publication.getJournal())
                .firstAuthor(publication.getFirstAuthor())
                .publicationDate(publication.getPublicationDate())
                .correspondingAuthor(
                        publication.getCorrespondingAuthor() != null ?
                                new CorrespondingAuthorDto(publication.getCorrespondingAuthor().getAuthorName(),
                                        publication.getCorrespondingAuthor().getEmail()) : null
                )
                .status(publication.getStatus())
                .curationStatus(publication.getCurationStatusId() != null ? curationStatusDTOAssembler.assemble(curationStatusService.findCurationStatus(publication.getCurationStatusId())) : null)
                .curator(publication.getCuratorId() != null ? curatorDTOAssembler.assemble(curatorService.findCuratorDetails(publication.getCuratorId())): null)
                .submitter(publication.getSubmitter())
                .build();

        final ControllerLinkBuilder controllerLinkBuilder = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(PublicationsController.class).getPublication(publication.getId())
        );

        Resource<PublicationDto> resource = new Resource<>(publicationDto);
        resource.add(BackendUtil.underBasePath(controllerLinkBuilder, depositionCurationConfig.getProxy_prefix()).withRel(DepositionCurationConstants.LINKS_PARENT));
        return resource;
    }
}
