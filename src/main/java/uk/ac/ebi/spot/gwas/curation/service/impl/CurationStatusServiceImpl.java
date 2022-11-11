package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.CurationStatusRepository;
import uk.ac.ebi.spot.gwas.curation.service.CurationStatusService;
import uk.ac.ebi.spot.gwas.deposition.domain.CurationStatus;

import java.util.Optional;

@Service
public class CurationStatusServiceImpl implements CurationStatusService {

    @Autowired
    CurationStatusRepository curationStatusRepository;

    @Override
    public Page<CurationStatus> findAllCurationStatus(Pageable pageable) {
        return curationStatusRepository.findAll(pageable);
    }

    @Override
    public CurationStatus findCurationStatus(String id) {
        return Optional.ofNullable(curationStatusRepository.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse(null);
    }
}
