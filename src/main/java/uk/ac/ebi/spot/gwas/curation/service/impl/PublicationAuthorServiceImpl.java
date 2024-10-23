package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationAuthorRepository;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationAuthorsSortRepository;
import uk.ac.ebi.spot.gwas.curation.rest.dto.PublicationAuthorDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.PublicationAuthorService;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuthor;
import uk.ac.ebi.spot.gwas.deposition.domain.PublicationAuthorsSort;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.PublicationAuthorDto;
import uk.ac.ebi.spot.gwas.deposition.europmc.EuropePMCData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PublicationAuthorServiceImpl implements PublicationAuthorService {

    @Autowired
    PublicationAuthorDtoAssembler publicationAuthorDtoAssembler;

    @Autowired
    PublicationAuthorRepository publicationAuthorRepository;

    @Autowired
    PublicationAuthorsSortRepository publicationAuthorsSortRepository;

    public List<String>  addAuthorsForPublication(EuropePMCData europePMCData, User user, String publicationId) {

        List<String> authorIds = new ArrayList<>();
        Integer order = 0;
        for(PublicationAuthorDto author : europePMCData.getAuthors()) {
            order++;
            PublicationAuthor publicationAuthor =  findUniqueAuthor(publicationAuthorDtoAssembler.disassemble(author, user));
            if(publicationAuthor == null){
                PublicationAuthor pubAuthor = publicationAuthorDtoAssembler.disassemble(author, user);
                pubAuthor.setCreated(new Provenance(DateTime.now(), user.getId()));
                PublicationAuthor author1 = savePublicationAuthor(pubAuthor);
                authorIds.add(author1.getId());
                setSort(publicationId, author1.getId(), order);
            } else {
                authorIds.add(publicationAuthor.getId());
                setSort(publicationId, publicationAuthor.getId(), order);
            }
        }
        return authorIds;

    }

    public String getFirstAuthorDetails(PublicationAuthorDto publicationAuthorDto, User user) {
        PublicationAuthor publicationAuthor =  findUniqueAuthor(publicationAuthorDtoAssembler.disassemble(publicationAuthorDto, user));
        return publicationAuthor.getId();
    }

    public PublicationAuthor findUniqueAuthor(PublicationAuthor publicationAuthor) {
      Optional<PublicationAuthor> optionalPublicationAuthor =  publicationAuthorRepository.findByFullNameAndFirstNameAndLastNameAndInitialsAndAffiliation(
                publicationAuthor.getFullName(), publicationAuthor.getFirstName(), publicationAuthor.getLastName(),
                publicationAuthor.getInitials(),publicationAuthor.getAffiliation());
      if(optionalPublicationAuthor.isPresent())
          return optionalPublicationAuthor.get();
      else
         return  null;
    }

    public PublicationAuthor savePublicationAuthor(PublicationAuthor publicationAuthor) {
        return publicationAuthorRepository.save(publicationAuthor);
    }

    public Optional<PublicationAuthor> getAuthorDetail(String seqId) {
        return publicationAuthorRepository.findById(seqId);
    }

    private void setSort(String pubId, String authorId, Integer sort) {
        PublicationAuthorsSort publicationAuthorsSort =    publicationAuthorsSortRepository.findByPublicationIdAndAuthorIdAndSort
                (pubId, authorId, sort).orElse(null);
        if(publicationAuthorsSort == null) {
            publicationAuthorsSort = new PublicationAuthorsSort(pubId, authorId, sort);
            publicationAuthorsSortRepository.save(publicationAuthorsSort);
        }
    }



}
