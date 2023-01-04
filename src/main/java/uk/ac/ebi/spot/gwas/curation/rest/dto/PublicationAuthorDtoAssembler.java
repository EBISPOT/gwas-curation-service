package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuthor;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.PublicationAuthorDto;

@Component
public class PublicationAuthorDtoAssembler {

    @Autowired
    ProvenanceDtoAssembler provenanceDtoAssembler;

    public  PublicationAuthorDto assemble(PublicationAuthor publicationAuthor, User user) {
        return  new PublicationAuthorDto(publicationAuthor.getFullName(),
                publicationAuthor.getFullNameStandard(),
                publicationAuthor.getFirstName(),
                publicationAuthor.getLastName(),
                publicationAuthor.getInitials(),
                publicationAuthor.getAffiliation(),
                publicationAuthor.getOrcid(),
                publicationAuthor.getCreated() != null ? ProvenanceDtoAssembler.assemble(publicationAuthor.getCreated(), user) : null,
                publicationAuthor.getUpdated() != null ? ProvenanceDtoAssembler.assemble(publicationAuthor.getUpdated(), user) : null);
    }

    public PublicationAuthor disassemble(PublicationAuthorDto publicationAuthorDto, User user) {
        return  new PublicationAuthor(publicationAuthorDto.getFullName(),
                publicationAuthorDto.getFullNameStandard(),
                publicationAuthorDto.getFirstName(),
                publicationAuthorDto.getLastName(),
                publicationAuthorDto.getInitials(),
                publicationAuthorDto.getAffiliation(),
                publicationAuthorDto.getOrcid(),
                publicationAuthorDto.getCreated() != null ? provenanceDtoAssembler.disassemble(publicationAuthorDto.getCreated(), user) : null,
                publicationAuthorDto.getUpdated() != null ? provenanceDtoAssembler.disassemble(publicationAuthorDto.getUpdated(), user) : null);
    }



}
