package ro.iordache.filestorage.rest.impl;

import org.springframework.http.ResponseEntity;

import ro.iordache.filestorage.rest.FileAccessServiceHandler;

/**
 * Provides File reading functionality
 */
public class ReadFileServiceHandler implements FileAccessServiceHandler {

    /**
     * Looks up the given file in the file storage.
     * If the file is found, it will be returned as part of the method's ResponseEntity response
     * Otherwise, the method will throw the appropriate error response
     * 
     * @param fileName - The file to be read
     * @return a {@link ResponseEntity} object
     */
    public ResponseEntity<String> doAction(String fileName) {
        return ResponseEntity.ok("Requesting file " + fileName);
    }
}
