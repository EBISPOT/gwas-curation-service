package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationNotes;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationNotesDto;

import java.util.List;

public interface PublicationNotesService {

    PublicationNotes createNotes(PublicationNotesDto notesDto, User user, String pubId);


    PublicationNotes getNotes(String pubId);

}
