package uk.ac.ebi.spot.gwas.curation.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.ebi.spot.gwas.curation.service.FtpService;
import uk.ac.ebi.spot.gwas.curation.util.CurationUtil;
import uk.ac.ebi.spot.gwas.deposition.config.FtpConfig;
import uk.ac.ebi.spot.gwas.deposition.exception.FileProcessingException;

import java.io.*;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class FtpServiceImpl implements FtpService {

    @Autowired
    private FtpConfig ftpConfig;

    @Override
    public String uploadAndGetFileName(MultipartFile multipartFile, String destDir) {

        AtomicReference<String> fileName = new AtomicReference<>("");
        Optional.of(multipartFile).ifPresent(file -> {
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());
            log.info("Original Filename: {}", baseName);
            fileName.set(String.format("%s_%s.%s", baseName, UUID.randomUUID(), extension));
            try {
                uploadToFtp(multipartFile.getInputStream(), fileName.get(), destDir);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
        return fileName.get();
    }


    public void uploadWeeklyStasFiles(File file) {
        int year = DateTime.now().year().get();
        String destDir = String.format("%s/%s", ftpConfig.getWeeklyStatsFolder(), year);
        String destination = String.format("%s/%s", destDir, file.getName());
        FTPClient client = connectToFtp();
        try {
            if (!client.changeWorkingDirectory(destDir)) {
                client.makeDirectory(destDir);
            }
            client.storeFile(destination, Files.newInputStream(file.toPath()));
            client.logout();
        } catch (IOException ex) {
            log.error("IO Exception in  uploadWeeklyStasFiles()", ex.getMessage(),ex);
        }

    }

    @Override
    public void uploadToFtp(InputStream inputStream, String fileName, String destDir) {
        String destination = String.format("%s/%s/%s", ftpConfig.getAppFolder(), destDir, fileName);
        log.info("File uploaded to ftp at destination {}", destination);

        FTPClient client = connectToFtp();
        try {
            client.changeWorkingDirectory(ftpConfig.getAppFolder());
            client.makeDirectory(destDir);
            client.storeFile(destination, inputStream);
            client.logout();
        } catch (IOException e) {
            throw new FileProcessingException("File was not uploaded to FTP");
        }
        log.info("Successfully Uploaded to Destination: {}", destination);
    }

    @Override
    public InputStreamResource downloadFile(String fileName, String subFolder) {
        String destination = String.format("%s/%s", ftpConfig.getAppFolder(), subFolder);
        FTPClient client = connectToFtp();
        try {
            client.changeWorkingDirectory(destination);
            InputStream inputStream = client.retrieveFileStream(fileName);
            InputStreamResource resource = new InputStreamResource(inputStream);
            log.info("{} file is downloaded", fileName);
            return resource;
        } catch (IOException e) {
            log.error("something went wrong {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean deleteFile(String fileName, String subFolder) {
        String destination = String.format("%s/%s", ftpConfig.getAppFolder(), subFolder);
        FTPClient client = connectToFtp();
        boolean deleted;
        try {
            client.changeWorkingDirectory(destination);
            deleted = client.deleteFile(fileName);
            if (!deleted){
                log.info("{} file was not deleted", fileName);
            }else {
                log.info("{} file was deleted", fileName);
            }
        } catch (IOException e) {
            throw new FileProcessingException(String.format("File was not deleted, due to error: %s", e.getMessage()));
        }
        return deleted;
    }

    @Override
    public FTPClient connectToFtp() {
        FTPClient client = new FTPClient();
        try {
            client.connect(ftpConfig.getFtpLink());
            if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
                log.info("Operation failed. Server reply code: {}", client.getReplyCode());
                client.disconnect();
            }
            boolean success = client.login(ftpConfig.getFtpUser(), ftpConfig.getFtpPass());
            if (!success) {
                client.disconnect();
            }
            client.setFileType(FTP.BINARY_FILE_TYPE);
            client.enterLocalPassiveMode();
        } catch (UnknownHostException E) {
            log.info("No such ftp server");
        } catch (IOException e) {
            log.info(e.getMessage());
        }
        return client;
    }
}

