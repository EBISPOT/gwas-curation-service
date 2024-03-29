package uk.ac.ebi.spot.gwas.curation.rest.dto;


import uk.ac.ebi.spot.gwas.deposition.domain.FileUpload;
import uk.ac.ebi.spot.gwas.deposition.dto.FileUploadDto;
import uk.ac.ebi.spot.gwas.deposition.dto.summarystats.SummaryStatsStatusDto;

import java.util.List;

public class FileUploadDtoAssembler {

    public static FileUploadDto assemble(FileUpload fileUpload,
                                         List<SummaryStatsStatusDto> summaryStatsStatuses) {
        return new FileUploadDto(fileUpload.getId(),
                fileUpload.getStatus(),
                fileUpload.getType(),
                fileUpload.getFileName(),
                fileUpload.getFileSize(),
                fileUpload.getErrors(),
                summaryStatsStatuses);
    }
}
