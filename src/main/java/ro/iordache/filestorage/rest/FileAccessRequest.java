package ro.iordache.filestorage.rest;

import java.io.InputStream;

/**
 * File access request model
 */
public class FileAccessRequest {
    
    private String fileName;
    
    private InputStream inputStream;
    
    private long ifModifiedSince;

    public FileAccessRequest (String fileName, InputStream is, long ifModifiedSince) {
        this.fileName = fileName;
        this.inputStream = is;
        this.ifModifiedSince = ifModifiedSince;
    }
    
    public String getFileName() {
        return this.fileName;
    }
    
    public InputStream getInputStream() {
        return this.inputStream;
    }
    
    public boolean checkNotModified(long lastModified) {
        if (this.ifModifiedSince > 0 && lastModified <= this.ifModifiedSince) {
            return true;
        }
        return false;
    }
}
