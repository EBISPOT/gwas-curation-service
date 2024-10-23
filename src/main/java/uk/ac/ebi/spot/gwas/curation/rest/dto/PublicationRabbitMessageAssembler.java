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

import java.util.Map;
import java.util.stream.Collectors;
@Component
public class PublicationRabbitMessageAssembler {

    @Autowired
    PublicationAuthorRepository publicationAuthorRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PublicationAuthorDtoAssembler publicationAuthorDtoAssembler;


   public PublicationRabbitMessage assemble(Publication publication, Map<Integer, PublicationAuthor> authorSortMap,
                                            PublicationAuthor firstAuthor,
                                            User user) {
       return PublicationRabbitMessage.builder()
               .pmid(publication.getPmid())
               .authors(authorSortMap.entrySet().stream()
                       .collect(Collectors.toMap(Map.Entry::getKey,
                               e -> publicationAuthorDtoAssembler.assemble(e.getValue(), user))))
               .firstAuthor(publicationAuthorDtoAssembler.assemble(firstAuthor, user))
               .publicationDate(CurationUtil.convertLocalDateToString(publication.getPublicationDate()))
               .title(publication.getTitle())
               .journal(publication.getJournal())
               .build();
   }


}
