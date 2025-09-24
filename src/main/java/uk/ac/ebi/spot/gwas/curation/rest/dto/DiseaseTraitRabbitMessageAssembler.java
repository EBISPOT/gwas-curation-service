package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitRabbitMessage;

@Component
public class DiseaseTraitRabbitMessageAssembler {

    public DiseaseTraitRabbitMessage assemble(DiseaseTrait diseaseTrait, String operation) {
        return DiseaseTraitRabbitMessage.builder()
                .trait(diseaseTrait.getTrait())
                .mongoSeqId(diseaseTrait.getId())
                .operation(operation)
                .build();

    }
}

