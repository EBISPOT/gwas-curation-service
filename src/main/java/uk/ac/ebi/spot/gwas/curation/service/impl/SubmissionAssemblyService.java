package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.config.DepositionCurationConfig;
import uk.ac.ebi.spot.gwas.curation.rest.SubmissionsController;
import uk.ac.ebi.spot.gwas.curation.rest.dto.*;
import uk.ac.ebi.spot.gwas.curation.service.BodyOfWorkService;
import uk.ac.ebi.spot.gwas.curation.service.FileUploadsService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.curation.util.BackendUtil;
import uk.ac.ebi.spot.gwas.deposition.constants.SubmissionProvenanceType;
import uk.ac.ebi.spot.gwas.deposition.domain.BodyOfWork;
import uk.ac.ebi.spot.gwas.deposition.domain.FileUpload;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.Submission;
import uk.ac.ebi.spot.gwas.deposition.dto.FileUploadDto;
import uk.ac.ebi.spot.gwas.deposition.dto.LockDetailsDto;
import uk.ac.ebi.spot.gwas.deposition.dto.SubmissionDto;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Service
public class SubmissionAssemblyService implements ResourceAssembler<Submission, Resource<SubmissionDto>> {

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private FileUploadsService fileUploadsService;

    @Autowired
    private UserService userService;

    @Autowired
    DepositionCurationConfig depositionCurationConfig;

    @Autowired
    private BodyOfWorkService bodyOfWorkService;

    @Autowired
    PublicationDtoAssembler publicationDtoAssembler;

    @Override
    public Resource<SubmissionDto> toResource(Submission submission) {
        Publication publication = null;
        BodyOfWork bodyOfWork = null;
        if (submission.getProvenanceType().equalsIgnoreCase(SubmissionProvenanceType.PUBLICATION.name())) {
            publication = publicationService.getPublicationDetailsByPmidOrPubId(submission.getPublicationId(), false);
        } else {
            if (!submission.getBodyOfWorks().isEmpty()) {
                bodyOfWork = bodyOfWorkService.retrieveBodyOfWork(submission.getBodyOfWorks().get(0));
            }
            if (submission.getPublicationId() != null) {
                publication = publicationService.getPublicationDetailsByPmidOrPubId(submission.getPublicationId(), false);
            }
        }
        List<FileUpload> fileUploads = fileUploadsService.getFileUploads(submission.getFileUploads());

        List<FileUploadDto> fileUploadDtos = new ArrayList<>();
        for (FileUpload fileUpload : fileUploads) {
            fileUploadDtos.add(FileUploadDtoAssembler.assemble(fileUpload, null));
        }

        SubmissionDto submissionDto = SubmissionDtoAssembler.assemble(submission,
                publication != null ? publicationDtoAssembler.assemble(publication, userService.getUser(submission.getCreated().getUserId())) : null,
                bodyOfWork != null ? BodyOfWorkDtoAssembler.assemble(bodyOfWork) : null,
                fileUploadDtos,
                ProvenanceDtoAssembler.assemble(submission.getCreated(), userService.getUser(submission.getCreated().getUserId())),
                submission.getLastUpdated() != null ?
                        ProvenanceDtoAssembler.assemble(submission.getLastUpdated(), userService.getUser(submission.getLastUpdated().getUserId())) :
                        ProvenanceDtoAssembler.assemble(submission.getCreated(), userService.getUser(submission.getCreated().getUserId())),
                submission.getEditTemplate() !=null ?
                        ProvenanceDtoAssembler.assemble(submission.getEditTemplate(), userService.getUser(submission.getEditTemplate().getUserId())):null,
                submission.getLockDetails() !=null?
                        new LockDetailsDto(ProvenanceDtoAssembler.assemble(submission.getLockDetails().getLockedBy(),
                                userService.getUser(submission.getLockDetails().getLockedBy().getUserId())),
                                submission.getLockDetails().getStatus()):null
        );

        final ControllerLinkBuilder lb = ControllerLinkBuilder.linkTo(
                methodOn(SubmissionsController.class).getSubmission(submission.getId()));

        Resource<SubmissionDto> resource = new Resource<>(submissionDto);
        resource.add(BackendUtil.underBasePath(lb, depositionCurationConfig.getProxy_prefix()).withSelfRel());

        return resource;
    }
}
