package ro.iordache.filestorage.rest.impl;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import jakarta.servlet.http.HttpServletRequest;
import ro.iordache.filestorage.rest.FileAccessServiceHandler;

/**
 * Provides File reading functionality
 */
@Service
public class PutFileServiceHandler implements FileAccessServiceHandler {

    private static final Logger logger = LoggerFactory.getLogger(PutFileServiceHandler.class);
    
    @Autowired
    private FileSystemStorageHelperImpl fileResolver;
    
    /**
     * Creates or updates a file in the file storage.
     * 
     * @param fileName - The file to be read
     * @param request - file contents
     * @return a {@link ResponseEntity} object depending on the outcome of the attempt to store the file
     */
    public ResponseEntity doAction(String fileName, HttpServletRequest request) {
        logger.debug("Handling PUT request for file {}", fileName);
        
        try {
            ResponseEntity response;
            
            File destinationFile = fileResolver.getStorageFile(fileName);
            if (destinationFile.exists()) {
                URI destURI = URI.create(request.getRequestURI());
                response = ResponseEntity.created(destURI).build();
            } else {
                response = ResponseEntity.ok().build();
            }
            logger.debug("Payload is " + request);
            FileCopyUtils.copy(request.getInputStream(), 
                    Files.newOutputStream(Paths.get(destinationFile.getAbsolutePath())));
            //requestBody.transferTo(destinationFile);
            return response;
        } catch (Exception e) {
            logger.error("Error storing file in the file system!", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
