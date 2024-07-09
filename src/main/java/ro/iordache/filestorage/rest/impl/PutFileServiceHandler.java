package ro.iordache.filestorage.rest.impl;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import ro.iordache.filestorage.repository.FileSystemStorageService;
import ro.iordache.filestorage.rest.FileAccessServiceHandler;

/**
 * PUT REST service handler -- provides file creating and updating functionality
 */
@Service
public class PutFileServiceHandler implements FileAccessServiceHandler {

    private static final Logger logger = LoggerFactory.getLogger(PutFileServiceHandler.class);
    
    @Autowired
    private FileSystemStorageService storageService;
    
    /**
     * Creates or updates a file in the file storage.
     * 
     * @param fileName - The file to be read
     * @param request - Current request containing the file contents
     * @return a {@link ResponseEntity} object depending on the outcome of the attempt to store the file
     */
    public ResponseEntity doAction(String fileName, HttpServletRequest request) {
        logger.debug("Handling PUT request for file {}", fileName);
        
        try {
            //TODO validate request
            ResponseEntity response;
            boolean isNewFile = storageService.storeFile(fileName, request.getInputStream());
            URI destURI = URI.create(request.getRequestURI());
            
            if (isNewFile) {
                response = ResponseEntity.created(destURI).build();
                logger.debug("PUT - creating new file {}", fileName);
            } else {
                response = ResponseEntity.ok().build();
                logger.debug("PUT - destination file {} already exists, updating it", fileName);
            }
            
            return response;
        } catch (Exception e) {
            logger.error("PUT - Error storing file in the file system!", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
