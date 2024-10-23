package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationAuthorRepository;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationAuthorsSortRepository;
import uk.ac.ebi.spot.gwas.curation.service.PublicationRabbitMessageService;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuthor;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuthorsSort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PublicationRabbitMessageServiceImpl implements PublicationRabbitMessageService {

    @Autowired
    PublicationAuthorRepository publicationAuthorRepository;

    @Autowired
    PublicationAuthorsSortRepository publicationAuthorsSortRepository;

    public Map<Integer, PublicationAuthor> getAuthorDetails(List<String> authorIds, String publicationId) {
        Map<Integer, PublicationAuthor> authorSortMap = new HashMap<>();
        for(String authorId : authorIds) {
            PublicationAuthor publicationAuthor =  publicationAuthorRepository.findById(authorId).orElse(null);
            PublicationAuthorsSort publicationAuthorsSort =  publicationAuthorsSortRepository.findByPublicationIdAndAuthorId(publicationId, authorId)
                    .orElse(null);
            authorSortMap.put(publicationAuthorsSort.getSort() , publicationAuthor);
        }
        return  authorSortMap;
    }


   public  PublicationAuthor getFirstAuthor(String authorId) {
       Optional<PublicationAuthor> optionalPublicationAuthor = publicationAuthorRepository.findById(authorId);
       if(optionalPublicationAuthor.isPresent())
           return optionalPublicationAuthor.get();
       else
           return null;
    }


}
