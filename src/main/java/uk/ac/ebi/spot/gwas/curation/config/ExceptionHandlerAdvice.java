package uk.ac.ebi.spot.gwas.curation.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.spot.gwas.curation.service.impl.ConversionJaversServiceImpl;
import uk.ac.ebi.spot.gwas.deposition.exception.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@ControllerAdvice(annotations = RestController.class)
public class ExceptionHandlerAdvice {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<String> handleAuthorizationException(AuthorizationException e) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException e) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<String> handleFileProcessingException(FileProcessingException ex) {
        log.error("FileProcessingException :"+ex.getMessage(),ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoVersionSummaryException.class)
    public ResponseEntity<String> handleNoVersionSummaryException(NoVersionSummaryException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JsonPatchException.class)
    public ResponseEntity<String> handleJsonPatchException(JsonPatchException ex) {
        log.error("JsonPatchException ->"+ex.getMessage(),ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<String> handleJsonProcessingException(JsonProcessingException ex) {
        log.error("JsonProcessingException ->"+ex.getMessage(),ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleMongoException(DataAccessException ex) {
        log.error("MongoException ->"+ex.getMessage(),ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CannotDeleteTraitException.class)
    public ResponseEntity<String> handleCannotDeleteTraitException(CannotDeleteTraitException ex) {
        log.error("CannotDeleteTraitException ->"+ex.getMessage(),ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException ex) {
        log.error("IOException ->"+ex.getMessage(),ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<String> handleExecutionException(ExecutionException ex) {
        log.error("ExecutionException ->"+ex.getMessage(),ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<String> handleInterruptedException(InterruptedException ex) {
        log.error("InterruptedException ->"+ex.getMessage(),ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("Exception ->"+ex.getMessage(),ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
