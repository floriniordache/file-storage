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
     * @param fileAccessRequest - a {@link FileAccessRequest} containing the required file details
     * @return a {@link ResponseEntity} object
     */
    public ResponseEntity doAction(FileAccessRequest fileAccessRequest);
}
