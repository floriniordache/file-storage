package ro.iordache.filestorage.rest;

/**
 * Interface for a generic file access operation
 */
public interface FileAccessServiceHandler {
    
    /**
     * File access operation type for this handler
     * 
     * @return a {@link FileAccessOperation} value
     */
    public FileAccessOperation getOperationType();
    
    /**
     * Performs an action on a file
     * 
     * @param fileAccessRequest - a {@link FileAccessRequest} containing the required file details
     * @return a {@link FileAccessResult} object
     */
    public FileAccessResult doAction(FileAccessRequest fileAccessRequest);
}
