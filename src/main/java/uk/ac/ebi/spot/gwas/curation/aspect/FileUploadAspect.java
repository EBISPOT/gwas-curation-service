package uk.ac.ebi.spot.gwas.curation.aspect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.apache.commons.io.FilenameUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.util.FileHandler;
import uk.ac.ebi.spot.gwas.deposition.domain.DiseaseTrait;
import uk.ac.ebi.spot.gwas.deposition.domain.EfoTrait;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.AnalysisDTO;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.AnalysisRequestDTO;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.DiseaseTraitWrapperDTO;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.EFOTraitWrapperDTO;
import uk.ac.ebi.spot.gwas.deposition.exception.FileProcessingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Aspect
@Component
public class FileUploadAspect {

    private static final Logger log = LoggerFactory.getLogger(FileHandler.class);


    @Pointcut("execution(* uk.ac.ebi.spot.gwas.curation.util.FileHandler.disassemble(..)) ")
    public void disassemble() {

    }

    @Pointcut("execution(* uk.ac.ebi.spot.gwas.curation.rest.dto.DiseaseTraitDtoAssembler.disassemble(org.springframework.web.multipart.MultipartFile)) ")
    public void disassembleDiseaseTraits() {

    }

    @Pointcut("execution(* uk.ac.ebi.spot.gwas.curation.rest.dto.EfoTraitDtoAssembler.disassemble(org.springframework.web.multipart.MultipartFile)) ")
    public void disassembleEFOTraits() {

    }

    @Pointcut("execution(* uk.ac.ebi.spot.gwas.curation.util.FileHandler.serializeDiseaseTraitAnalysisFile(..)) ")
    public void disassembleDiseaseTraitsAnalysis() {

    }

    @Pointcut("execution(* uk.ac.ebi.spot.gwas.curation.service.DiseaseTraitService.similaritySearch(..)) ")
    public void similaritySearch(){

    }


    @SuppressWarnings("unchecked")
    @Around("disassembleDiseaseTraitsAnalysis()")
    public Object disassembleMultipartFileTraitsAnalysis(ProceedingJoinPoint joinPoint) throws Throwable {
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        List<AnalysisDTO> analysisDTOS;
        MultipartFile  multipartFile = (MultipartFile) args.get(0);
        validateFileExtension(multipartFile);
        AnalysisRequestDTO obj = new AnalysisRequestDTO("");
        String validationSepMessage = parseFileForSeparators(multipartFile.getInputStream(), "\t",  obj);
        log.info("validationSepMessage -> "+ validationSepMessage);
        if(!validationSepMessage.equals("Done"))
            throw new FileProcessingException(validationSepMessage);
        else
            analysisDTOS  = (List<AnalysisDTO>) joinPoint.proceed();
        return analysisDTOS;
    }

    @SuppressWarnings("unchecked")
    @Around("disassembleEFOTraits()")
    public Object disassembleMultipartFileEFOTraits(ProceedingJoinPoint joinPoint) throws Throwable {
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        List<EfoTrait> efos;
        MultipartFile  multipartFile = (MultipartFile) args.get(0);
        validateFileExtension(multipartFile);
        EFOTraitWrapperDTO obj = new EFOTraitWrapperDTO("","");
        String validationSepMessage = parseFileForSeparators(multipartFile.getInputStream(), "\t",  obj);
        log.info("validationSepMessage -> "+ validationSepMessage);
        if(!validationSepMessage.equals("Done"))
            throw new FileProcessingException(validationSepMessage);
        else
            efos  = (List<EfoTrait>) joinPoint.proceed();
        return efos;
    }

    @SuppressWarnings("unchecked")
    @Around("disassembleDiseaseTraits()")
    public Object disassembleMultipartFileDiseaseTraits(ProceedingJoinPoint joinPoint) throws Throwable {
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        List<DiseaseTrait> diseaseTraits;
        MultipartFile  multipartFile = (MultipartFile) args.get(0);
        validateFileExtension(multipartFile);
        DiseaseTraitWrapperDTO obj = new DiseaseTraitWrapperDTO("");
        String validationSepMessage = parseFileForSeparators(multipartFile.getInputStream(), "\t",  obj);
        if(!validationSepMessage.equals("Done"))
            throw new FileProcessingException(validationSepMessage);
        else
            diseaseTraits = (List<DiseaseTrait>) joinPoint.proceed();
        return diseaseTraits;
    }

    @SuppressWarnings("unchecked")
    @Around("disassemble()")
    public <T> List<T> disassembleMultipartFileValidation(ProceedingJoinPoint joinPoint) throws Throwable {
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        MultipartFile  multipartFile = (MultipartFile) args.get(0);
        Object obj = args.get(2);
        List<T> objects;
        validateFileExtension(multipartFile);
        String validationSepMessage = parseFileForSeparators(multipartFile.getInputStream(), "\t",  obj);
        log.info("validationSepMessage -> "+ validationSepMessage);
        if(!validationSepMessage.equals("Done"))
            throw   new FileProcessingException(validationSepMessage);
        else
          objects =  (List<T>)  joinPoint.proceed();

        return objects;
    }

    @SuppressWarnings("unchecked")
    @Around("similaritySearch()")
    public Object similaritySearchResponseTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = timeNow();
        Object object= joinPoint.proceed();
        log.info("Time taken for Similarity Analysis is ->"+(timeNow() - startTime));
        return object;
    }



    public  String parseFileForSeparators(InputStream is, String sep, Object T) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String str = "";
        int counter = 0;
        CsvMapper csvMapper = new CsvMapper();
        Map<String, Object> dataList = csvMapper.convertValue(T, new TypeReference<Object>() {
        });
        List<String> headers = new ArrayList<>();
        dataList.forEach((key, value) ->
                headers.add(key));
        headers.forEach(header -> log.info("Header is ->"+ header));
        try {
            while ((str = bufferedReader.readLine()) != null) {
                counter++;
                //log.info("File Line number " + counter + "->" + str);
                String[] props = str.split(sep);
                AtomicBoolean invalidHeader = new AtomicBoolean();
                invalidHeader.set(false);
                if(counter == 1){
                    if(props != null) {
                        Arrays.stream(props).forEach(prop -> {
                            if (!headers.contains(prop)){
                                invalidHeader.set(true);
                                return;
                            }
                        });
                    }
                }
                if(invalidHeader.get()){
                    return "Headers are not valid , the expected headers->"+headers.stream().collect(Collectors.joining("\t"));
                }
                //log.info("props length " + props.length);
                //log.info("allFields length " + headers.size());
                if(props != null ) {
                    if (props.length != headers.size() ) {
                        return "Line Number " + counter + " doesn't have valid file separator or number of fields";
                    }
                }else{
                    return "Line Number " + counter + " doesn't have valid file separator or number of fields";
                }
            }
        } catch(IOException ex ){
            log.error("IO Exception in reading file "+ ex, ex.getMessage());
        } finally{
            try{
                if(is != null)
                    is.close();
            }catch(IOException ex){
                log.error("IO Exception in closing file "+ ex, ex.getMessage());
            }
        }
        return "Done";
    }


    public void validateFileExtension(MultipartFile multipartFile) {
        if (!FilenameUtils.getExtension(multipartFile.getOriginalFilename()).equals("tsv")) {
            throw new FileProcessingException("File Uploaded should be of tsv format");
        }
    }

    private long timeNow(){
        return System.currentTimeMillis();
    }

}
