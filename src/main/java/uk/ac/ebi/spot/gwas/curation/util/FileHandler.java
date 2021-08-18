package uk.ac.ebi.spot.gwas.curation.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.AnalysisDTO;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.FileUploadRequest;
import uk.ac.ebi.spot.gwas.deposition.exception.FileProcessingException;
import uk.ac.ebi.spot.gwas.deposition.exception.FileValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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


    public static List<AnalysisDTO> serializeDiseaseTraitAnalysisFile(FileUploadRequest fileUploadRequest) {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = getSchemaFromMultiPartFile(fileUploadRequest.getMultipartFile());
        List<AnalysisDTO> analysisDTOS;
        try {
            InputStream inputStream = fileUploadRequest.getMultipartFile().getInputStream();
            MappingIterator<AnalysisDTO> iterator =
                    mapper.readerFor(AnalysisDTO.class).with(schema).readValues(inputStream);
            analysisDTOS = iterator.readAll();
        } catch (IOException e) {
            throw new FileProcessingException("Could not read the file");
        }
        return analysisDTOS;
    }


    public static byte[] serializePojoToTsv(List<?> pojoList) {
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
}
