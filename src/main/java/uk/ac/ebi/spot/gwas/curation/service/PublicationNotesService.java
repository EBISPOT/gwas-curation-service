package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationNotes;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationNotesDto;

import java.util.List;

public interface PublicationNotesService {

    PublicationNotes createNotes(PublicationNotesDto notesDto, User user, String pubId);


    Page<PublicationNotes> getNotes(String pubId, Pageable pageable);

    PublicationNotes updateNotes(PublicationNotesDto publicationNotesDto, User user, String pubId, String noteId);

    void deleteNotes(String pubId, String noteId);

}
