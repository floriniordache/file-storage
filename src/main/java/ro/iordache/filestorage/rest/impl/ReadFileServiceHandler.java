package ro.iordache.filestorage.rest.impl;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import ro.iordache.filestorage.repository.FileSystemStorageService;
import ro.iordache.filestorage.rest.FileAccessServiceHandler;

/**
 * Provides File reading functionality
 */
@Service
public class ReadFileServiceHandler implements FileAccessServiceHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReadFileServiceHandler.class);
    
    @Autowired
    private FileSystemStorageService storageService;
    
    /**
     * Looks up the given file in the file storage.
     * If the file is found, it will be returned as part of the method's ResponseEntity response
     * Otherwise, the method will return the appropriate error response
     * 
     * @param fileName - The file to be read
     * @param request - not used
     * @return a {@link ResponseEntity} object
     */
    public ResponseEntity doAction(String fileName, HttpServletRequest request) {
        logger.debug("Handling READ request for file {}", fileName);
        
        try {
            InputStream foundFileIS = storageService.getFileContent(fileName);
            if (foundFileIS == null) {
                return ResponseEntity.notFound().build();
            }
            
            InputStreamResource fileISResource = new InputStreamResource(foundFileIS);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(fileISResource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        
    }
}
