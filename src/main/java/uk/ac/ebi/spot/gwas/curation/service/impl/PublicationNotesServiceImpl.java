package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationNotesRepository;
import uk.ac.ebi.spot.gwas.curation.rest.dto.PublicationNotesDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.PublicationNotesService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationNotes;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationNotesDto;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;

import java.util.Optional;

@Service
public class PublicationNotesServiceImpl implements PublicationNotesService {

    @Autowired
    PublicationNotesRepository publicationNotesRepository;

    @Autowired
    PublicationNotesDtoAssembler publicationNotesDtoAssembler;

    @Autowired
    UserService userService;

    @Autowired
    PublicationService publicationService;

    public PublicationNotes createNotes(PublicationNotesDto publicationNotesDto, User user, String pubId) {
        Publication publication = publicationService.getPublicationDetailsByPmidOrPubId(pubId,false);
        if(publication == null) {
            throw new EntityNotFoundException("PublicationId not found");
        }
        publicationNotesDto.setPublicationId(publication.getId());
        PublicationNotes publicationNotes = publicationNotesDtoAssembler.disassemble(publicationNotesDto, user);
        return publicationNotesRepository.insert(publicationNotes);
    }

    public Page<PublicationNotes> getNotes(String pubId, Pageable pageable) {

        return publicationNotesRepository.findByPublicationId(pubId, pageable);

    }

    public PublicationNotes updateNotes(PublicationNotesDto publicationNotesDto, User user, String pubId, String noteId) {
        Publication publication = publicationService.getPublicationDetailsByPmidOrPubId(pubId,false);
        PublicationNotes publicationNotes = null;
        if(publication == null) {
            throw new EntityNotFoundException("PublicationId not found"+pubId);
        }
        Optional<PublicationNotes> optPubExists = publicationNotesRepository.findById(noteId);
        if (optPubExists.isPresent()) {
            publicationNotes = optPubExists.get();
            publicationNotes.setNotes(publicationNotesDto.getNotes());
            publicationNotes.setUpdated(new Provenance(DateTime.now(), user.getId()));
        } else {
            throw new EntityNotFoundException("Publication Note  not found "+noteId);
        }
        return publicationNotesRepository.save(publicationNotes);
    }

    public void deleteNotes(String pubId, String noteId) {
        Publication publication = publicationService.getPublicationDetailsByPmidOrPubId(pubId,false);
        if(publication == null) {
            throw new EntityNotFoundException("PublicationId not found"+pubId);
        }
        Optional<PublicationNotes> optPubExists = publicationNotesRepository.findById(noteId);
        if (optPubExists.isPresent()) {
            publicationNotesRepository.delete(optPubExists.get());
        }else {
            throw new EntityNotFoundException("Publication Note  not found "+noteId);
        }
    }

}
