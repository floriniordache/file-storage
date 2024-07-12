package ro.iordache.filestorage.rest.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ro.iordache.filestorage.repository.FileSystemStorageService;
import ro.iordache.filestorage.rest.FileAccessOperation;
import ro.iordache.filestorage.rest.FileAccessRequest;
import ro.iordache.filestorage.rest.FileAccessResult;
import ro.iordache.filestorage.rest.FileAccessServiceHandler;

/**
 * Provides File deleting functionality
 */
@Service
public class DeleteFileServiceHandler implements FileAccessServiceHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeleteFileServiceHandler.class);
    
    @Autowired
    private FileSystemStorageService storageService;
    
    public FileAccessOperation getOperationType() {
        return FileAccessOperation.DELETE;
    }
    
    /**
     * Looks up the given file in the file storage. If the file is found, it will be deleted
     * 
     * @param fileAccessRequest - The {@link FileAccessRequest} object with details on the file to be deleted from the storage
     * @return a {@link FileAccessResult} object
     */
    public FileAccessResult doAction(FileAccessRequest fileAccessRequest) {
        logger.debug("Handling DELETE request for file {}", fileAccessRequest.getFileName());
        
        try {
            boolean deleteSuccess = storageService.deleteFile(fileAccessRequest.getFileName());
            
            if (!deleteSuccess) {
                return FileAccessResult.build(FileAccessResult.NOT_FOUND);
            }
            return FileAccessResult.build(FileAccessResult.OK);
        } catch (Exception e) {
            logger.error("Error deleting file!", e);
            return FileAccessResult.build(FileAccessResult.INTERNAL_ERROR);
        }
        
    }
}
