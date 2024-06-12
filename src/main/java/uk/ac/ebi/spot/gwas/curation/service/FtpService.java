package uk.ac.ebi.spot.gwas.curation.service;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FtpService {

    String uploadAndGetFileName(MultipartFile multipartFile, String destDir);

    void uploadToFtp(InputStream inputStream, String fileName, String destDir);

    InputStreamResource downloadFile(String fileName, String subFolder);

    FTPClient connectToFtp();
}
