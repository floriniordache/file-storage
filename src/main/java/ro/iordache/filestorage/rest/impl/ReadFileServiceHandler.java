package ro.iordache.filestorage.rest.impl;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import ro.iordache.filestorage.repository.FileSystemStorageService;
import ro.iordache.filestorage.rest.FileAccessOperation;
import ro.iordache.filestorage.rest.FileAccessRequest;
import ro.iordache.filestorage.rest.FileAccessResult;
import ro.iordache.filestorage.rest.FileAccessServiceHandler;

/**
 * Provides File reading functionality
 */
@Service
public class ReadFileServiceHandler implements FileAccessServiceHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReadFileServiceHandler.class);
    
    @Autowired
    private FileSystemStorageService storageService;
    
    public FileAccessOperation getOperationType() {
        return FileAccessOperation.READ;
    }
    
    /**
     * Looks up the given file in the file storage.
     * 
     * fileAccessRequest - the {@link FileAccessRequest} for the file to be fetched from the storage
     * @return a {@link FileAccessResult} object
     */
    public FileAccessResult doAction(FileAccessRequest fileAccessRequest) {
        logger.debug("Handling READ request for file {}", fileAccessRequest.getFileName());
        
        try {
            InputStream foundFileIS = storageService.getFileContent(fileAccessRequest.getFileName());
            if (foundFileIS == null) {
                return FileAccessResult.build(FileAccessResult.NOT_FOUND);
            }
            
            InputStreamResource fileISResource = new InputStreamResource(foundFileIS);
            return FileAccessResult.build(FileAccessResult.OK, fileISResource);
        } catch (Exception e) {
            logger.error("Error reading file from the internal storage!", e);
            return FileAccessResult.build(FileAccessResult.INTERNAL_ERROR);
        }
        
    }
}
