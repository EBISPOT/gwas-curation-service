package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EfoTraitRabbitMessage;

@Component
public class EFOTraitRabbitMessageAssembler {

  public EfoTraitRabbitMessage assemble(EfoTrait efoTrait, String operation) {
        return EfoTraitRabbitMessage.builder()
                .trait(efoTrait.getTrait())
                .shortForm(efoTrait.getShortForm())
                .uri(efoTrait.getUri())
                .mongoSeqId(efoTrait.getId())
                .operation(operation)
                .build();
    }
}
