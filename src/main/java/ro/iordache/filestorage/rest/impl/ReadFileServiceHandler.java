package ro.iordache.filestorage.rest.impl;

import java.io.File;
import java.io.FileInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import ro.iordache.filestorage.repository.util.FileSystemStorageHelperImpl;
import ro.iordache.filestorage.rest.FileAccessServiceHandler;

/**
 * Provides File reading functionality
 */
@Service
public class ReadFileServiceHandler implements FileAccessServiceHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReadFileServiceHandler.class);
    
    @Autowired
    private FileSystemStorageHelperImpl fileResolver;
    
    /**
     * Looks up the given file in the file storage.
     * If the file is found, it will be returned as part of the method's ResponseEntity response
     * Otherwise, the method will throw the appropriate error response
     * 
     * @param fileName - The file to be read
     * @param request - not used
     * @return a {@link ResponseEntity} object
     */
    public ResponseEntity doAction(String fileName, HttpServletRequest request) {
        logger.debug("Handling READ request for file {}", fileName);
        
        File resolvedFileToRead = fileResolver.findFile(fileName);
        if (resolvedFileToRead == null) {
            logger.debug("No file found with name {}", fileName);
            return ResponseEntity.notFound().build();
        }
        
        try {
            InputStreamResource fileISRes = new InputStreamResource(new FileInputStream(resolvedFileToRead));
            return ResponseEntity.ok()
                    .contentLength(resolvedFileToRead.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileISRes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        
    }
}
