package ro.iordache.filestorage.rest;

import org.springframework.core.io.InputStreamResource;

/**
 * File access operation result
 */
public class FileAccessResult extends RESTApiResult {
    // result InputStreamResource
    private InputStreamResource inputStreamResource;
    
    private FileAccessResult(int type, InputStreamResource is) {
        this.type = type;
        this.inputStreamResource = is;
    }
    
    public InputStreamResource getInputStream() {
        return this.inputStreamResource;
    }
    
    public static FileAccessResult build(int type, InputStreamResource is) {
        return new FileAccessResult(type, is);
    }

    
    public static FileAccessResult build(int type) {
        return new FileAccessResult(type, null);
    }
}
