package ro.iordache.filestorage.rest;

import java.io.InputStream;

/**
 * File access request model
 */
public class FileAccessRequest {
    
    private String fileName;
    
    private InputStream inputStream;
    
    private long modifiedSince;

    public FileAccessRequest (String fileName, InputStream is, long modifiedSince) {
        this.fileName = fileName;
        this.inputStream = is;
        this.modifiedSince = modifiedSince;
    }
    
    public String getFileName() {
        return this.fileName;
    }
    
    public InputStream getInputStream() {
        return this.inputStream;
    }
    
    /**
     * Checks a given timestamp against the modified sice timestamp
     * 
     * @param lastModified the timestamp to check against the modified since timestamp
     * @return true if the modifiedSince value is valid (>0) and the given lastModified if before the modified since value
     *  false otherwise
     */
    public boolean checkNotModified(long lastModified) {
        if (this.modifiedSince > 0 && lastModified <= this.modifiedSince) {
            return true;
        }
        return false;
    }
}
