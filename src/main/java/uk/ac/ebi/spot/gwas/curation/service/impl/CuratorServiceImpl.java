package uk.ac.ebi.spot.gwas.curation.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.gwas.curation.repository.CuratorRepository;
import uk.ac.ebi.spot.gwas.curation.service.CuratorService;
import uk.ac.ebi.spot.gwas.deposition.domain.Curator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public Curator findCuratorByLastName(String lastName) {
        return Optional
                .of(curatorRepository.findCuratorByLastName(lastName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElse(null);
    }


    public Map<String, String> getCuratorsMap() {
        Map<String, String> curatorsMap = new HashMap<>();
        List<Curator> curatorsList = curatorRepository.findAll();
        curatorsList.forEach(curator -> curatorsMap.put(curator.getId(), getCuratorName(curator)));
        return curatorsMap;
    }

    private String getCuratorName(Curator curator) {
        if(curator.getLastName() != null && curator.getFirstName() != null) {
            return String.format("%s %s",curator.getFirstName(),curator.getLastName());
        }else if(curator.getLastName() != null) {
            return curator.getLastName();
        }else {
            return curator.getFirstName();
        }
    }

}
