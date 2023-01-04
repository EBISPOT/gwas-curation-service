package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.service.PublicationAuthorService;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.CorrespondingAuthorDto;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationDto;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PublicationDtoAssembler  {

    @Autowired
    ProvenanceDtoAssembler provenanceDtoAssembler;

    @Autowired
    PublicationAuthorService publicationAuthorService;

    @Autowired
    PublicationAuthorDtoAssembler publicationAuthorDtoAssembler;

    public  PublicationDto assemble(Publication publication, User user) {
        return new PublicationDto(publication.getId(),
                publication.getPmid(),
                publication.getTitle(),
                publication.getJournal(),
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
                        .collect(Collectors.toList())
                ).orElse(null));
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

}
