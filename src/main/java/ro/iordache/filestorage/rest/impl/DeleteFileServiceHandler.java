package ro.iordache.filestorage.rest.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ro.iordache.filestorage.repository.FileSystemStorageService;
import ro.iordache.filestorage.rest.FileAccessRequest;
import ro.iordache.filestorage.rest.FileAccessServiceHandler;

/**
 * Provides File deleting functionality
 */
@Service
public class DeleteFileServiceHandler implements FileAccessServiceHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeleteFileServiceHandler.class);
    
    @Autowired
    private FileSystemStorageService storageService;
    
    /**
     * Looks up the given file in the file storage. If the file is found, it will be deleted
     * 
     * @param fileAccessRequest - The {@link FileAccessRequest} object with details on the file to be deleted from the storage
     * @return a {@link ResponseEntity} object which is either
     *      200 OK if file deletion is successful
     *      404 NOT FOUND if file is not present in the storage
     *      500 INTERNAL SERVER ERROR on any other error
     */
    public ResponseEntity doAction(FileAccessRequest fileAccessRequest) {
        logger.debug("Handling DELETE request for file {}", fileAccessRequest.getFileName());
        
        try {
            boolean deleteSuccess = storageService.deleteFile(fileAccessRequest.getFileName());
            
            if (!deleteSuccess) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        
    }
}
