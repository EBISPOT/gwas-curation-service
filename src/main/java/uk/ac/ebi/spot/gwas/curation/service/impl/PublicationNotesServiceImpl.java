package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationNotesRepository;
import uk.ac.ebi.spot.gwas.curation.rest.dto.PublicationNotesDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.PublicationNotesService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationService;
import uk.ac.ebi.spot.gwas.curation.service.UserService;
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
        if(publication != null) {
            publicationNotesDto.setPublicationId(publication.getId());
        } else{
            throw new EntityNotFoundException("PublicationId not found");
        }
        PublicationNotes publicationNotes = publicationNotesDtoAssembler.disassemble(publicationNotesDto, user);
        return publicationNotesRepository.save(publicationNotes);
    }

    public PublicationNotes getNotes(String pubId) {

        Optional<PublicationNotes> optionalPubNotes = publicationNotesRepository.findByPublicationId(pubId);
        if(optionalPubNotes.isPresent()) {
            return optionalPubNotes.get();
        }else{
            return null;
        }
    }
    


}
