package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.domain.CurationStatus;

import java.util.Map;

public interface CurationStatusService {

    Page<CurationStatus> findAllCurationStatus(Pageable pageable);

    CurationStatus findCurationStatus(String id);

    CurationStatus findCurationStatusByStatus(String status);

    CurationStatus createCurationStatus(String status);

     Map<String, String> getCurationStatusMap();



}
