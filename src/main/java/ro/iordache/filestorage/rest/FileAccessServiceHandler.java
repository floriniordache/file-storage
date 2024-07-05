package ro.iordache.filestorage.rest;

import org.springframework.http.ResponseEntity;

/**
 * Interface for a generic file access operation
 */
public interface FileAccessServiceHandler {
    
    /**
     * Performs an action on a file
     * 
     * @param fileName - The file on which we need to perform the action
     * @return a {@link ResponseEntity} object
     */
    public ResponseEntity<String> doAction(String fileName);
}
