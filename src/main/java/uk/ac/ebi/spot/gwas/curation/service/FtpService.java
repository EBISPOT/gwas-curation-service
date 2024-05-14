package uk.ac.ebi.spot.gwas.curation.service;

import org.apache.commons.net.ftp.FTPClient;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FtpService {

    public String uploadAndGetFileName(MultipartFile multipartFile, String destDir);

    public void uploadToFtp(InputStream inputStream, String fileName, String destDir);

    public InputStreamResource downloadFile(String fileName, String subFolder);

    public FTPClient connectToFtp();
}
