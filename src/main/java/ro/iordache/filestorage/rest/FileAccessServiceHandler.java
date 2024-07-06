package ro.iordache.filestorage.rest;

import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Interface for a generic file access operation
 */
public interface FileAccessServiceHandler {
    
    /**
     * Performs an action on a file
     * 
     * @param fileName - The file on which we need to perform the action
     * @param request - The request body
     * @return a {@link ResponseEntity} object
     */
    public ResponseEntity doAction(String fileName, HttpServletRequest request);
}
