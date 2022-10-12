package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.CuratorRepository;
import uk.ac.ebi.spot.gwas.curation.service.CuratorService;
import uk.ac.ebi.spot.gwas.deposition.domain.Curator;

import java.util.Optional;

@Service
public class CuratorServiceImpl implements CuratorService {

    @Autowired
    CuratorRepository curatorRepository;

    @Override
    public Page<Curator> findAllCurators(Pageable page) {
        return curatorRepository.findAll(page);
    }

    public Curator findCuratorDetails(String id) {
        return Optional.ofNullable(curatorRepository.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse(null);

    }

}
