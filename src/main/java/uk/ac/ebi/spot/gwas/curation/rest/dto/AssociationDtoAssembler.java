package uk.ac.ebi.spot.gwas.curation.rest.dto;


import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.gwas.deposition.domain.Association;
import uk.ac.ebi.spot.gwas.deposition.dto.AssociationDto;

@Component
public class AssociationDtoAssembler {



    public static AssociationDto assemble(Association association) {
       return new AssociationDto(association.getStudyTag(),
                association.getHaplotypeId(),
                association.getVariantId(),
                association.getPvalue(),
                association.getPvalueText(),
                association.getProxyVariant(),
                association.getEffectAllele(),
                association.getOtherAllele(),
                association.getEffectAlleleFrequency(),
                association.getOddsRatio(),
                association.getBeta(),
                association.getBetaUnit(),
                association.getCiLower(),
                association.getCiUpper(),
                association.getStandardError());
    }

    public static Association disassemble(AssociationDto associationDto) {
        Association association = new Association();
        association.setStudyTag(associationDto.getStudyTag());
        association.setHaplotypeId(associationDto.getHaplotypeId());
        association.setVariantId(associationDto.getVariantId());
        association.setPvalue(associationDto.getPvalue());
        association.setProxyVariant(associationDto.getProxyVariant());
        association.setPvalueText(associationDto.getPvalueText());
        association.setEffectAllele(associationDto.getEffectAllele());
        association.setOtherAllele(associationDto.getOtherAllele());
        association.setEffectAlleleFrequency(associationDto.getEffectAlleleFrequency());
        association.setOddsRatio(associationDto.getOddsRatio());
        association.setBeta(associationDto.getBeta());
        association.setBetaUnit(associationDto.getBetaUnit());
        association.setCiLower(associationDto.getCiLower());
        association.setCiUpper(associationDto.getCiUpper());
        association.setStandardError(associationDto.getStandardError());

        return association;
    }

}
