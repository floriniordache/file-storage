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
     * @return a {@link FileAccessResult} object
     */
    public FileAccessResult doAction(FileAccessRequest fileAccessRequest) {
        logger.debug("Handling PUT request for file {}", fileAccessRequest.getFileName());
        
        try {
            FileAccessResult response;
            boolean isNewFile = storageService.storeFile(fileAccessRequest.getFileName(), fileAccessRequest.getInputStream());
            
            if (isNewFile) {
                response = FileAccessResult.build(FileAccessResult.CREATED);
                logger.debug("PUT - creating new file {}", fileAccessRequest.getFileName());
            } else {
                response = FileAccessResult.build(FileAccessResult.OK);
                logger.debug("PUT - destination file {} already exists, updating it", fileAccessRequest.getFileName());
            }
            
            return response;
        } catch (Exception e) {
            logger.error("PUT - Error storing file in the file system!", e);
            return FileAccessResult.build(FileAccessResult.INTERNAL_ERROR);
        }
    }
}
