package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationAuthorRepository;
import uk.ac.ebi.spot.gwas.curation.repository.UserRepository;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.PublicationRabbitMessage;

import java.util.Optional;
import java.util.stream.Collectors;
@Component
public class PublicationRabbitMessageAssembler {

    @Autowired
    PublicationAuthorRepository publicationAuthorRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PublicationAuthorDtoAssembler publicationAuthorDtoAssembler;

   public PublicationRabbitMessage assemble(Publication publication) {
       return PublicationRabbitMessage.builder()
               .pmid(publication.getPmid())
               .authors( publication.getAuthors() != null ?
                       publication.getAuthors().stream()
                       .map(id -> publicationAuthorRepository.findById(id))
                       .filter(Optional::isPresent)
                       .map(Optional::get)
                       .map(pubAuthor ->  publicationAuthorDtoAssembler.assemble(pubAuthor,
                               userRepository.findById(pubAuthor.getCreated().getUserId()).get()))
                       .collect(Collectors.toList()) : null)
               .firstAuthor(publicationAuthorDtoAssembler.assemble( publicationAuthorRepository.
                                       findById(publication.getFirstAuthorId()).get()
                               , userRepository.findById(publication.getCreated().getUserId()).get()))
               .publicationDate(CurationUtil.convertLocalDateToString(publication.getPublicationDate()))
               .title(publication.getTitle())
               .journal(publication.getJournal())
               .build();
   }
}
