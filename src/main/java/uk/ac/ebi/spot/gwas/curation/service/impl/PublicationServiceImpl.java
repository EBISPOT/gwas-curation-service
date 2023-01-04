package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationRepository;
import uk.ac.ebi.spot.gwas.curation.rest.dto.PublicationDtoAssembler;
import uk.ac.ebi.spot.gwas.curation.service.EuropepmcPubMedSearchService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationAuthorService;
import uk.ac.ebi.spot.gwas.curation.service.PublicationService;
import uk.ac.ebi.spot.gwas.curation.solr.repository.PublicationSolrRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.PublicationDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.PublicationAuthorDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.PublicationStatusReport;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SearchPublicationDTO;
import uk.ac.ebi.spot.gwas.deposition.europmc.EuropePMCData;
import uk.ac.ebi.spot.gwas.deposition.exception.EuropePMCException;
import uk.ac.ebi.spot.gwas.deposition.exception.PubmedLookupException;
import uk.ac.ebi.spot.gwas.deposition.solr.SOLRPublication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                String pmid = searchPublicationDTO.getPmid();
                String curator = searchPublicationDTO.getCurator();
                String title = searchPublicationDTO.getTitle();
                String curationStatus = searchPublicationDTO.getCurationStatus();
                if (pmid != null && curator != null && curationStatus != null && title != null) {
                    return publicationSolrRepository.findByPmidAndCurationStatusAndCuratorAndTitle(pmid, curator, curationStatus, title,
                            page);
                } else if (pmid != null && curator != null && curationStatus != null) {
                    return publicationSolrRepository.findByPmidAndCurationStatusAndCurator(pmid, curationStatus, curator, page);
                } else if (pmid != null && title != null && curator != null) {
                    return publicationSolrRepository.findByPmidAndCuratorAndTitle(pmid, curator, title, page);
                } else if (pmid != null && title != null && curationStatus != null) {
                    return publicationSolrRepository.findByPmidAndCurationStatusAndTitle(pmid, curationStatus, title, page);
                } else if (curator != null && title != null && curationStatus != null) {
                    return publicationSolrRepository.findByCurationStatusAndCuratorAndTitle(curationStatus, curator, title, page);
                } else if (pmid != null && curationStatus != null) {
                    return publicationSolrRepository.findByPmidAndCurationStatus(pmid, curationStatus, page);
                } else if (pmid != null && curator != null) {
                    return publicationSolrRepository.findByPmidAndCurator(pmid, curator, page);
                } else if (pmid != null && title != null) {
                    return publicationSolrRepository.findByPmidAndTitle(pmid, title, page);
                }  else if (curationStatus != null && title != null) {
                    return publicationSolrRepository.findByCurationStatusAndTitle(curationStatus, title, page);
                } else if ( curator != null && title != null) {
                    return publicationSolrRepository.findByCuratorAndTitle(curator, title, page);
                }  else if ( curator != null && curationStatus != null) {
                    return publicationSolrRepository.findByCuratorAndCurationStatus(curator, curationStatus, page);
                } else if (pmid != null) {
                    return publicationSolrRepository.findByPmid(pmid, page);
                } else if (curator != null ){
                    return  publicationSolrRepository.findByCurator(curator, page);
                } else if (title != null ) {
                    return publicationSolrRepository.findByTitle(title, page);
                } else if (curationStatus != null ) {
                    return publicationSolrRepository.findByCurationStatus(curationStatus, page);
                }
            }

            return publicationSolrRepository.findAll(page);

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

}
