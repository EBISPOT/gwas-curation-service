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
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.spot.gwas.deposition.dto.curation.ErrorResponseDto;
import uk.ac.ebi.spot.gwas.deposition.exception.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


@ControllerAdvice(annotations = RestController.class)
public class ExceptionHandlerAdvice {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<String> handleAuthorizationException(AuthorizationException e) {
        log.error("AuthorizationException :"+e.getLocalizedMessage(),e);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException e) {

        log.error("EntityNotFoundException :"+e.getLocalizedMessage(),e);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ErrorResponseDto> handleFileProcessingException(FileProcessingException ex) {
        log.error("FileProcessingException :"+ex.getLocalizedMessage(),ex);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(new ErrorResponseDto(ex.getMessage()), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoVersionSummaryException.class)
    public ResponseEntity<String> handleNoVersionSummaryException(NoVersionSummaryException ex) {
        log.error("NoVersionSummaryException :"+ex.getLocalizedMessage(),ex);
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

    @ExceptionHandler(InvalidEFOUriException.class)
    public ResponseEntity<String> handleInvalidEFOUriException(InvalidEFOUriException ex) {
        log.error("InvalidEFOUriException ->"+ex.getMessage(),ex);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CannotCreateTraitWithDuplicateNameException.class)
    public ResponseEntity<String> handleCreateTraitWithDuplicateName(CannotCreateTraitWithDuplicateNameException e) {
        log.error("Exception -> " + e.getMessage(), e);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CannotCreateTraitWithDuplicateUriException.class)
    public ResponseEntity<String> handleCreateTraitWithDuplicateUri(CannotCreateTraitWithDuplicateUriException e) {
        log.error("Exception -> " + e.getMessage(), e);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CannotCreateTraitWithDuplicateShortFormException.class)
    public ResponseEntity<String> handleCreateTraitWithDuplicateShortForm(CannotCreateTraitWithDuplicateShortFormException e) {
        log.error("Exception -> " + e.getMessage(), e);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(StudiesWithoutTraitsException.class)
    public ResponseEntity<String> hanldeStudiesWithoutTraits(StudiesWithoutTraitsException e){
        log.error("Exception -> " + e.getMessage(), e);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TraitsNotSyncedException.class)
    public ResponseEntity<String> hanldeTraitsNotSynced(TraitsNotSyncedException e){
        log.error("Exception -> " + e.getMessage(), e);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(e.getMessage(), headers, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("Exception :"+ex.getLocalizedMessage(),ex);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(ex.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
