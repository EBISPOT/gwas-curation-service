package uk.ac.ebi.spot.gwas.curation.rest.dto;

import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.domain.User;
import uk.ac.ebi.spot.gwas.deposition.dto.UserDto;

@Component
public class UserDtoAssembler {


    public UserDto assemble(User user) {
        return new UserDto(user.getName(), user.getEmail(),
                user.getNickname(), user.getUserReference(), user.getDomains());
    }
}
