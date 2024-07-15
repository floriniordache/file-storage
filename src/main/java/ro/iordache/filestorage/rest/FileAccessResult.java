package ro.iordache.filestorage.rest;

import org.springframework.core.io.InputStreamResource;

/**
 * File access operation result
 */
public class FileAccessResult extends RESTApiResult {
    private InputStreamResource inputStreamResource;
    
    private long lastModified;
    
    private FileAccessResult(int type, InputStreamResource is, long lastModified) {
        this.type = type;
        this.inputStreamResource = is;
        this.lastModified = lastModified;
    }
    
    public InputStreamResource getInputStream() {
        return this.inputStreamResource;
    }
    
    public long getLastModified() {
        return this.lastModified;
    }
    
    public static FileAccessResult build(int type, InputStreamResource is, long lastModified) {
        return new FileAccessResult(type, is, lastModified);
    }

    
    public static FileAccessResult build(int type) {
        return new FileAccessResult(type, null, -1);
    }
}
