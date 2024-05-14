package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Streamable;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.constants.DepositionCurationConstants;
import uk.ac.ebi.spot.gwas.curation.repository.BodyOfWorkRepository;
import uk.ac.ebi.spot.gwas.curation.repository.SubmissionRepository;
import uk.ac.ebi.spot.gwas.curation.rest.PublicationsController;
import uk.ac.ebi.spot.gwas.curation.service.CurationStatusService;
import uk.ac.ebi.spot.gwas.curation.service.CuratorService;

import uk.ac.ebi.spot.gwas.curation.service.PublicationAuthorService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.BodyOfWork;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.Submission;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.CorrespondingAuthorDto;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationDto;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PublicationDtoAssembler implements ResourceAssembler<Publication, Resource<PublicationDto>> {

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

    @Autowired
    private BodyOfWorkRepository bodyOfWorkRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    public PublicationDto assemble(Publication publication, User user) {
        return PublicationDto.builder()
                .publicationId(publication.getId())
                .pmid(publication.getPmid())
                .journal(publication.getJournal())
                .title(publication.getTitle())
                .firstAuthor(publication.getFirstAuthor())
                .publicationDate(publication.getPublicationDate())
                .correspondingAuthor(publication.getCorrespondingAuthor() != null ?
                        new CorrespondingAuthorDto(
                                publication.getCorrespondingAuthor().getAuthorName(),
                                publication.getCorrespondingAuthor().getEmail()
                        ) : null)
                .status(publication.getStatus())
                .created(publication.getCreated() != null ? ProvenanceDtoAssembler.assemble(publication.getCreated(), user): null)
                .updated(publication.getUpdated() != null ? ProvenanceDtoAssembler.assemble(publication.getUpdated(), user): null)
                .authors(Optional.ofNullable(publication.getAuthors())
                        .filter(authors -> !authors.isEmpty())
                        .map(authors -> authors.stream().map(authorId -> publicationAuthorService.getAuthorDetail(authorId))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(author -> publicationAuthorDtoAssembler.assemble(author, user))
                                .collect(Collectors.toList())
                        )
                        .orElse(null)
                )
                .curationStatus(publication.getCurationStatusId() != null ? curationStatusDTOAssembler.assemble(curationStatusService.findCurationStatus(publication.getCurationStatusId())) : null)
                .curator(publication.getCuratorId() != null ? curatorDTOAssembler.assemble(curatorService.findCuratorDetails(publication.getCuratorId())): null)
                .submitter(publication.getSubmitter())
                .bodyOfWorkId(bodyOfWorkRepository.findByPmidsContains(publication.getPmid()).map(BodyOfWork::getId).orElse(null))
                .submissionIds(Optional.ofNullable(submissionRepository.findByPublicationIdAndArchived(publication.getId(), false, Pageable.unpaged()))
                        .map(submissions -> submissions
                                .stream()
                                // METADATA before SS
                                .sorted(Comparator.comparing(Submission::getType))
                                .map(Submission::getId)
                                .collect(Collectors.toList())
                        )
                        .orElse(null)
                )
                .isUserRequested(publication.getIsUserRequested())
                .isOpenTargets(publication.getIsOpenTargets())
                .build();
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
                null,
                publicationDto.getIsUserRequested(),
                publicationDto.getIsOpenTargets()
        );
    }

    @Override
    public Resource<PublicationDto> toResource(Publication publication) {
        PublicationDto publicationDto = assemble(publication, new User());

        final ControllerLinkBuilder controllerLinkBuilder = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(PublicationsController.class).getPublication(publication.getId())
        );

        Resource<PublicationDto> resource = new Resource<>(publicationDto);
        resource.add(BackendUtil.underBasePath(controllerLinkBuilder, depositionCurationConfig.getProxy_prefix()).withRel(DepositionCurationConstants.LINKS_PARENT));
        return resource;
    }
}
