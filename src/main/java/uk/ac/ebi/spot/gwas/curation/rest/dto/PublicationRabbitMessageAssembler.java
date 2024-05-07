package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationAuthorRepository;
import uk.ac.ebi.spot.gwas.curation.repository.UserRepository;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuthor;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.PublicationRabbitMessage;

import java.util.List;
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

   public PublicationRabbitMessage assemble(Publication publication, List<PublicationAuthor> authors,
                                            PublicationAuthor firstAuthor, User user) {
       return PublicationRabbitMessage.builder()
               .pmid(publication.getPmid())
               .authors(authors.stream()
                       .map(pubAuthor ->  publicationAuthorDtoAssembler.assemble(pubAuthor,user))
                       .collect(Collectors.toList()))
               .firstAuthor(publicationAuthorDtoAssembler.assemble(firstAuthor, user))
               .publicationDate(CurationUtil.convertLocalDateToString(publication.getPublicationDate()))
               .title(publication.getTitle())
               .journal(publication.getJournal())
               .build();
   }
}
