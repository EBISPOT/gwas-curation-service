package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationAuthorRepository;
import uk.ac.ebi.spot.gwas.curation.service.PublicationRabbitMessageService;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuthor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PublicationRabbitMessageServiceImpl implements PublicationRabbitMessageService {

    @Autowired
    PublicationAuthorRepository publicationAuthorRepository;

    public List<PublicationAuthor> getAuthorDetails(List<String> authorIds) {
       return  (ArrayList) publicationAuthorRepository.findAllById(authorIds);
    }


   public  PublicationAuthor getFirstAuthor(String authorId) {
       Optional<PublicationAuthor> optionalPublicationAuthor = publicationAuthorRepository.findById(authorId);
       if(optionalPublicationAuthor.isPresent())
           return optionalPublicationAuthor.get();
       else
           return null;
    }

}
