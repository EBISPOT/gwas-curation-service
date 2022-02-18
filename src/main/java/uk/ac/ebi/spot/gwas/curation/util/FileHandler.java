package uk.ac.ebi.spot.gwas.curation.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.constants.FileUploadType;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.*;
import uk.ac.ebi.spot.gwas.deposition.exception.FileProcessingException;
import uk.ac.ebi.spot.gwas.deposition.exception.FileValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class FileHandler {

    private FileHandler() {
        // Hide implicit default constructor
    }

    public static CsvSchema getSchemaFromMultiPartFile(MultipartFile multipartFile){
        CsvSchema.Builder builder = CsvSchema.builder();
        CsvSchema schema = builder.build().withHeader();
        if (FilenameUtils.getExtension(multipartFile.getOriginalFilename()).equals("tsv")) {
            schema = schema.withColumnSeparator('\t');
        }
        return schema;
    }


    public  List<AnalysisDTO> serializeDiseaseTraitAnalysisFile(MultipartFile multipartFile) {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = getSchemaFromMultiPartFile(multipartFile);
        List<AnalysisDTO> analysisDTOS;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            MappingIterator<AnalysisDTO> iterator =
                    mapper.readerFor(AnalysisDTO.class).with(schema).readValues(inputStream);
            analysisDTOS = iterator.readAll();
        } catch (IOException e) {
            throw new FileProcessingException("Could not read the file");
        }
        return analysisDTOS;
    }


    public  byte[] serializePojoToTsv(List<?> pojoList) {
        CsvMapper csvMapper = new CsvMapper();
        List<Map<String, Object>> dataList = csvMapper.convertValue(pojoList, new TypeReference<Object>() {
        });
        List<List<String>> csvData = new ArrayList<>();
        List<String> csvHead = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger();
        dataList.forEach(row -> {
            List<String> rowData = new ArrayList<>();
            row.forEach((key, value) -> {
                rowData.add(String.valueOf(value));
                if (counter.get() == 0) {
                    csvHead.add(key);
                }
            });
            csvData.add(rowData);
            counter.getAndIncrement();
        });
        CsvSchema.Builder builder = CsvSchema.builder();
        csvHead.forEach(builder::addColumn);
        CsvSchema schema = builder.build().withHeader().withLineSeparator("\n").withColumnSeparator('\t');
        //String result = "";
        byte[] result;
        try {
           // result = csvMapper.writer(schema).writeValueAsString(csvData);
            result = csvMapper.writer(schema).writeValueAsBytes(csvData);
        } catch (IOException e) {
            throw new FileProcessingException("Could not read the file");
        }
        return result;
    }

    public  String getTemplate(String fileUploadType) {
        if (fileUploadType.equals(FileUploadType.SIMILARITY_ANALYSIS_FILE)) {
            List<AnalysisDTO> analysisDTO = new ArrayList<>();
            analysisDTO.add(AnalysisDTO.builder().userTerm("Yeast Infection").build());
            analysisDTO.add(AnalysisDTO.builder().userTerm("mean interproximal clinical attachment level").build());
            return new String(serializePojoToTsv(analysisDTO));
        } else if (fileUploadType.equals(FileUploadType.REPORTED_TRAIT_FILE)) {

            List<DiseaseTraitWrapperDTO> diseaseTraitDtos = new ArrayList<>();
            diseaseTraitDtos.add(DiseaseTraitWrapperDTO.builder().trait("Uterine Carcinoma").build());
            diseaseTraitDtos.add(DiseaseTraitWrapperDTO.builder().trait("Malaria Parasite").build());
            return new String(serializePojoToTsv(diseaseTraitDtos));
        } else if (fileUploadType.equals(FileUploadType.STUDY_TRAIT_FILE)){
            List<StudyPatchRequest> studyPatchRequestList = new ArrayList<>();
            studyPatchRequestList.add(StudyPatchRequest.builder().gcst("GCST90000026").curatedReportedTrait("Kashin-Beck disease").build());
            studyPatchRequestList.add(StudyPatchRequest.builder().gcst("GCST90000029").curatedReportedTrait("Shingles").build());
            studyPatchRequestList.add(StudyPatchRequest.builder().gcst("GCST90000028").curatedReportedTrait("Bilirubin levels in extreme obesity").build());
            studyPatchRequestList.add(StudyPatchRequest.builder().gcst("GCST90000030").curatedReportedTrait("Hepatic lipid content in extreme obesity").build());
            return new String(serializePojoToTsv(studyPatchRequestList));
        } else if (fileUploadType.equals(FileUploadType.EFO_TRAIT_FILE)) {
            List<EFOTraitWrapperDTO> efoTraitDtos = new ArrayList<>();
            efoTraitDtos.add(EFOTraitWrapperDTO.builder().trait("uterine carcinoma").uri("http://www.ebi.ac.uk/efo/EFO_0002919").build());
            efoTraitDtos.add(EFOTraitWrapperDTO.builder().trait("malaria").uri("http://www.ebi.ac.uk/efo/EFO_0001068").build());
            return new String(serializePojoToTsv(efoTraitDtos));
        } else if (fileUploadType.equals(FileUploadType.STUDY_EFO_TRAIT_FILE)) {
            List<EfoTraitStudyMappingDto> efoTraitStudyMappingDtos = new ArrayList<>();
            efoTraitStudyMappingDtos.add(EfoTraitStudyMappingDto.builder().gcst("GCST90000026").shortForm("EFO_0001060").build());
            efoTraitStudyMappingDtos.add(EfoTraitStudyMappingDto.builder().gcst("GCST90000029").shortForm("EFO_0001065").build());
            efoTraitStudyMappingDtos.add(EfoTraitStudyMappingDto.builder().gcst("GCST90000028").shortForm("EFO_0001068").build());
            efoTraitStudyMappingDtos.add(EfoTraitStudyMappingDto.builder().gcst("GCST90000030").shortForm("EFO_0001071").build());
            return new String(serializePojoToTsv(efoTraitStudyMappingDtos));
        }
        else {
            return null;
        }
    }
}
