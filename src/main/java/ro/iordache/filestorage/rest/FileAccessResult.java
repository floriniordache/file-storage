package ro.iordache.filestorage.rest;

import java.net.URI;

import org.springframework.core.io.InputStreamResource;

/**
 * File access operation result
 */
public class FileAccessResult {

    // result types
    public static final int OK = 0;
    public static final int NOT_FOUND = 1;
    public static final int CREATED = 2;
    public static final int INTERNAL_ERROR = 3;
    
    // type of this result
    private int type;
    
    // result URI
    private URI uri;
    
    // result InputStreamResource
    private InputStreamResource inputStreamResource;
    
    private FileAccessResult(int type, URI uri, InputStreamResource is) {
        this.type = type;
        this.uri = uri;
        this.inputStreamResource = is;
    }
    
    public int getType() {
        return this.type;
    }
    
    public URI getURI() {
        return this.uri;
    }
    
    public InputStreamResource getInputStream() {
        return this.inputStreamResource;
    }
    
    public static FileAccessResult build(int type, URI uri, InputStreamResource is) {
        return new FileAccessResult(type, uri, is);
    }
    
    public static FileAccessResult build(int type, InputStreamResource is) {
        return new FileAccessResult(type, null, is);
    }
    
    public static FileAccessResult build(int type, URI uri) {
        return new FileAccessResult(type, uri, null);
    }
    
    public static FileAccessResult build(int type) {
        return new FileAccessResult(type, null, null);
    }
}
