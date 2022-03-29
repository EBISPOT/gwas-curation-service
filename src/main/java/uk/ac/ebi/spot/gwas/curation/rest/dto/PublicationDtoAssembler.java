package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.dto.CorrespondingAuthorDto;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationDto;

@Component
public class PublicationDtoAssembler  {



    public static PublicationDto assemble(Publication publication) {
        return new PublicationDto(publication.getId(),
                publication.getPmid(),
                publication.getTitle(),
                publication.getJournal(),
                publication.getFirstAuthor(),
                publication.getPublicationDate(),
                publication.getCorrespondingAuthor() != null ?
                        new CorrespondingAuthorDto(publication.getCorrespondingAuthor().getAuthorName(),
                                publication.getCorrespondingAuthor().getEmail()) : null,
                publication.getStatus());
    }


}
