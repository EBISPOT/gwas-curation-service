package uk.ac.ebi.spot.gwas.curation.rest.dto;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EfoTraitStudyMappingDto;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.StudyPatchRequest;
import uk.ac.ebi.spot.gwas.deposition.exception.FileProcessingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class StudyPatchRequestAssembler {

    public List<StudyPatchRequest> disassemble(MultipartFile multipartFile) {
        CsvMapper mapper = new CsvMapper();
        CsvSchema csvSchema = FileHandler.getSchemaFromMultiPartFile(multipartFile);
        List<StudyPatchRequest> studyPatchRequestList;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            MappingIterator<StudyPatchRequest> iterator = mapper.readerFor(StudyPatchRequest.class).with(csvSchema).readValues(inputStream);
            studyPatchRequestList = iterator.readAll();
        }catch (IOException ex){
            throw new FileProcessingException("Could not read the file");
        }
        return studyPatchRequestList;
    }

    public List<EfoTraitStudyMappingDto> disassembleForEfoMapping(MultipartFile multipartFile) {

        CsvMapper mapper = new CsvMapper();
        CsvSchema csvSchema = FileHandler.getSchemaFromMultiPartFile(multipartFile);
        List<EfoTraitStudyMappingDto> studyPatchRequestList;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            MappingIterator<EfoTraitStudyMappingDto> iterator = mapper.readerFor(EfoTraitStudyMappingDto.class).with(csvSchema).readValues(inputStream);
            studyPatchRequestList = iterator.readAll();
        } catch (IOException ex){
            throw new FileProcessingException("Could not read the file");
        }
        return studyPatchRequestList;
    }

}
