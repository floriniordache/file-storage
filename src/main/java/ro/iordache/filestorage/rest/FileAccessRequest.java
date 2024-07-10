package ro.iordache.filestorage.rest;

import java.io.InputStream;

/**
 * File access request model
 */
public class FileAccessRequest {
    
    private String fileName;
    
    private InputStream inputStream;

    public FileAccessRequest (String fileName, InputStream is) {
        this.fileName = fileName;
        this.inputStream = is;
    }
    
    public String getFileName() {
        return this.fileName;
    }
    
    public InputStream getInputStream() {
        return this.inputStream;
    }
}
