package uk.ac.ebi.spot.gwas.curation.rest.dto;


import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.domain.Provenance;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.ProvenanceDto;
import uk.ac.ebi.spot.gwas.deposition.dto.UserDto;

@Component
public class ProvenanceDtoAssembler {

    public static ProvenanceDto assemble(Provenance provenance, User user) {
        return new ProvenanceDto(new UserDto(user.getName(), user.getEmail(),
                user.getNickname(), user.getUserReference(), user.getDomains()),
                provenance.getTimestamp());
    }

    public  Provenance disassemble(ProvenanceDto provenanceDto, User user) {
        return new Provenance(provenanceDto.getTimestamp(), user.getId());
    }
}
