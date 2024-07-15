package ro.iordache.filestorage.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Generic REST API result
 */
public abstract class RESTApiResult {
    // result types
    public static final int OK = 0;
    public static final int NOT_FOUND = 1;
    public static final int CREATED = 2;
    public static final int INTERNAL_ERROR = 3;
    public static final int NOT_MODIFIED = 4;
    
    // type of this result
    @JsonIgnore
    protected int type;
    
    public int getType() {
        return this.type;
    }
}
