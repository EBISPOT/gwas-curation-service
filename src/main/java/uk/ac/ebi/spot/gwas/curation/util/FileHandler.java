package uk.ac.ebi.spot.gwas.curation.util;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

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
}
