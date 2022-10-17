package uk.ac.ebi.spot.gwas.curation.service.impl;

import jdk.nashorn.internal.runtime.options.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.PublicationRepository;
import uk.ac.ebi.spot.gwas.curation.service.PublicationService;
import uk.ac.ebi.spot.gwas.curation.solr.repository.PublicationSolrRepository;
import uk.ac.ebi.spot.gwas.deposition.domain.Publication;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.SearchPublicationDTO;
import uk.ac.ebi.spot.gwas.deposition.solr.SOLRPublication;

import java.util.Optional;

@Service
public class PublicationServiceImpl implements PublicationService {

    @Autowired
    PublicationRepository publicationRepository;

    @Autowired
    PublicationSolrRepository publicationSolrRepository;

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

}
