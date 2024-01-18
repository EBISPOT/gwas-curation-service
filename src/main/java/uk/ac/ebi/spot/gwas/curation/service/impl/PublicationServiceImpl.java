package uk.ac.ebi.spot.gwas.curation.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.Document;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationRepository;
import uk.ac.ebi.spot.gwas.curation.rest.dto.PublicationDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.*;
import uk.ac.ebi.spot.gwas.curation.solr.repository.PublicationSolrRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.PublicationAuthorDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.PublicationStatusReport;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SearchPublicationDTO;
import uk.ac.ebi.spot.gwas.deposition.europmc.EuropePMCData;
import uk.ac.ebi.spot.gwas.deposition.exception.EntityNotFoundException;
import uk.ac.ebi.spot.gwas.deposition.exception.EuropePMCException;
import uk.ac.ebi.spot.gwas.deposition.exception.PubmedLookupException;
import uk.ac.ebi.spot.gwas.deposition.solr.SOLRPublication;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class PublicationServiceImpl implements PublicationService {

    private static final Logger log = LoggerFactory.getLogger(PublicationServiceImpl.class);

    @Autowired
    PublicationRepository publicationRepository;

    @Autowired
    PublicationSolrRepository publicationSolrRepository;

    @Autowired
    EuropepmcPubMedSearchService europepmcPubMedSearchService;

    @Autowired
    PublicationDtoAssembler publicationDtoAssembler;

    @Autowired
    PublicationAuthorService publicationAuthorService;

    @Autowired
    CuratorService curatorService;

    @Autowired
    CurationStatusService curationStatusService;

    @Override
    public Publication getPublicationDetailsByPmidOrPubId(String pmid, Boolean isPmid) {
        Publication publication = null;
        if(isPmid) {
            Optional<Publication>  optPublication = publicationRepository.findByPmid(pmid);
            if(optPublication.isPresent())
                publication = optPublication.get();
        }
        else {
            Optional<Publication> optPublication =  publicationRepository.findById(pmid);
            if(optPublication.isPresent())
                publication = optPublication.get();
        }
        return publication;
    }




    @Override
    public Page<SOLRPublication> searchPublications(SearchPublicationDTO searchPublicationDTO, Pageable page) {
            if (searchPublicationDTO != null ) {
                String pmid = searchPublicationDTO.getPmid() == null ? "*:*" : "pmid:" + searchPublicationDTO.getPmid();
                String curator = searchPublicationDTO.getCurator() == null ? "*:*" : "curator:*"+ searchPublicationDTO.getCurator() + "*";
                String title = searchPublicationDTO.getTitle() == null ? "*:*" : "title:*"+ searchPublicationDTO.getTitle() + "*";
                String curationStatus = searchPublicationDTO.getCurationStatus() == null ? "*:*" : "curationStatus:" + searchPublicationDTO.getCurationStatus();
                String submitter = searchPublicationDTO.getSubmitter() == null ? "*:*" : "submitter:*"+ searchPublicationDTO.getSubmitter() + "*";
                return publicationSolrRepository.findPublications(pmid, title, curator, curationStatus, submitter, page);
            }

            return publicationSolrRepository.findAll(page);
    }

    @Override
    public Page<Publication> search(SearchPublicationDTO searchPublicationDTO, Pageable pageable) throws IOException {
        List<String> queryList = new ArrayList<>();
        if (searchPublicationDTO.getPmid() != null) {
            queryList.add("\"pmid\": \"" + searchPublicationDTO.getPmid() + "\"");
        }
        if (searchPublicationDTO.getTitle() != null ) {
            queryList.add("\"title\": {\"$regex\": \".*" + searchPublicationDTO.getTitle() + ".*\", \"$options\" : \"i\"}");
        }
        if (searchPublicationDTO.getCurator() != null) {
            queryList.add("\"curatorId\": \"" + searchPublicationDTO.getPmid() + "\"");
        }
        if (searchPublicationDTO.getCurationStatus() != null) {
            queryList.add("\"curationStatusId\": \"" + searchPublicationDTO.getPmid() + "\"");
        }
        if (searchPublicationDTO.getSubmitter() != null) {
            queryList.add("\"submitter\": {\"$regex\": \".*" + searchPublicationDTO.getSubmitter() + ".*\", \"$options\" : \"i\"}");
        }
        String query = "{" + String.join(",", queryList) + "}";
        Map<String,Object> result = new ObjectMapper().readValue(query, HashMap.class);
        BSONObject bsonQuery = new BasicBSONObject();
        bsonQuery.putAll(result);
        return publicationRepository.findByQuery(bsonQuery, pageable);
    }

    @Override
    public SOLRPublication getPublicationFromSolr(String id) {
        return Optional.ofNullable(publicationSolrRepository.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse(null);

    }

    public  Publication importNewPublication(String pmid, User user) throws PubmedLookupException, EuropePMCException {

     EuropePMCData europePMCData = europepmcPubMedSearchService.createStudyByPubmed(pmid);
     PublicationDto publicationDto =  europePMCData.getPublication();
     Publication publication = publicationDtoAssembler.disassemble(publicationDto, user);
     publication.setAuthors(publicationAuthorService.
                addAuthorsForPublication(europePMCData , user));
     addFirstAuthorToPublication(publication, europePMCData,  user);
     return  publication;
    }

    public void addFirstAuthorToPublication(Publication publication, EuropePMCData europePMCData, User user) {
     PublicationAuthorDto publicationAuthorDto = europePMCData.getFirstAuthor();
     publication.setFirstAuthorId(publicationAuthorService.
                getFirstAuthorDetails(publicationAuthorDto , user));
     publication.setCreated(new Provenance(DateTime.now(), user.getId()));
     savePublication(publication);

    }

    public Publication savePublication(Publication publication) {
        return publicationRepository.save(publication);
    }



    public List<PublicationStatusReport>  createPublication(List<String> pmids, User user) {
        List<PublicationStatusReport> reports = new ArrayList<>();
        pmids.forEach( pmid -> {
            Publication publication = getPublicationDetailsByPmidOrPubId(pmid, true);
            if(publication != null) {
                PublicationStatusReport statusReport = new PublicationStatusReport();
                statusReport.setPmid(pmid);
                statusReport.setPublicationDto(publicationDtoAssembler.assemble(publication, user));
                statusReport.setStatus("Pmid already exists");
                reports.add(statusReport);
            } else {

                    try {
                        Publication publicationImported = importNewPublication(pmid, user);
                        PublicationStatusReport statusReport = new PublicationStatusReport();
                        statusReport.setPmid(pmid);
                        statusReport.setPublicationDto(publicationDtoAssembler.assemble(publicationImported, user));
                        statusReport.setStatus("Pmid Saved");
                        reports.add(statusReport);
                    } catch (EuropePMCException ex){
                        PublicationStatusReport statusReport = new PublicationStatusReport();
                        statusReport.setPmid(pmid);
                        statusReport.setStatus("EuropePMC API could not be contacted");
                        reports.add(statusReport);
                    }catch (PubmedLookupException ex) {
                        PublicationStatusReport statusReport = new PublicationStatusReport();
                        statusReport.setPmid(pmid);
                        statusReport.setStatus("Pmid not found in EuropePMC API");
                        reports.add(statusReport);
                    }

                }

        });
        return reports;
    }

    //adds curationStatus and assigns curator to publication
    @Override
    public PublicationDto addPublicationCurationDetails(String pubmedId, PublicationDto publicationDto, User user) {
        if ((publicationDto.getCurationStatus() == null || publicationDto.getCurationStatus().getId() == null)
                && (publicationDto.getCurator() == null || publicationDto.getCurator().getId() == null)) {
            throw new IllegalArgumentException("both curationStatus.id and curator.id are null, at least one required");
        }
        Publication publication = publicationRepository
                .findByPmid(pubmedId)
                .orElseThrow(() -> new EntityNotFoundException("publication id not found"));
        if (publicationDto.getCurationStatus() != null && publicationDto.getCurationStatus().getId() != null) {
            if (curationStatusService.findCurationStatus(publicationDto.getCurationStatus().getId()) == null) {
                throw new EntityNotFoundException("curationStatus.id not found");
            }
            publication.setCurationStatusId(publicationDto.getCurationStatus().getId());
        }
        if (publicationDto.getCurator() != null && publicationDto.getCurator().getId() != null) {
            if (curatorService.findCuratorDetails(publicationDto.getCurator().getId()) == null) {
                throw new EntityNotFoundException("curator.id not found");
            }
            publication.setCuratorId(publicationDto.getCurator().getId());
        }
        publication = publicationRepository.save(publication);
        return publicationDtoAssembler.assemble(publication, user);
    }

}
