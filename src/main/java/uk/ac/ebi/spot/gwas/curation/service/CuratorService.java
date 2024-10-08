package uk.ac.ebi.spot.gwas.curation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.ac.ebi.spot.gwas.deposition.domain.Curator;

import java.util.Map;

public interface CuratorService {

    Page<Curator> findAllCurators(Pageable page);

    Curator findCuratorDetails(String id);

    Curator findCuratorByLastName(String id);

    Map<String, String> getCuratorsMap();

}
