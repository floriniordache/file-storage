package ro.iordache.filestorage.rest.impl;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ro.iordache.filestorage.repository.FileSystemStorageService;
import ro.iordache.filestorage.rest.FileAccessOperation;
import ro.iordache.filestorage.rest.FileAccessRequest;
import ro.iordache.filestorage.rest.FileAccessServiceHandler;

/**
 * PUT REST service handler -- provides file creating and updating functionality
 */
@Service
public class PutFileServiceHandler implements FileAccessServiceHandler {

    private static final Logger logger = LoggerFactory.getLogger(PutFileServiceHandler.class);
    
    @Autowired
    private FileSystemStorageService storageService;
    
    public FileAccessOperation getOperationType() {
        return FileAccessOperation.CREATE_UPDATE;
    }
    
    /**
     * Creates or updates a file in the file storage.
     * 
     * @param fileAccessRequest - The {@link FileAccessRequest} with details on the file to be read
     * @return a {@link ResponseEntity} object depending on the outcome of the attempt to store the file
     */
    public ResponseEntity doAction(FileAccessRequest fileAccessRequest) {
        logger.debug("Handling PUT request for file {}", fileAccessRequest.getFileName());
        
        try {
            //TODO validate request
            ResponseEntity response;
            boolean isNewFile = storageService.storeFile(fileAccessRequest.getFileName(), fileAccessRequest.getInputStream());
            //URI destURI = URI.create(request.getRequestURI());
            
            if (isNewFile) {
                // TODO decouple the service handler from the response building
                response = ResponseEntity.created(new URI("/")).build();
                logger.debug("PUT - creating new file {}", fileAccessRequest.getFileName());
            } else {
                response = ResponseEntity.ok().build();
                logger.debug("PUT - destination file {} already exists, updating it", fileAccessRequest.getFileName());
            }
            
            return response;
        } catch (Exception e) {
            logger.error("PUT - Error storing file in the file system!", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
