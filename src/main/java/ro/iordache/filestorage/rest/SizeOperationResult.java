package ro.iordache.filestorage.rest;

/**
 * Repository size operation result
 */
public class SizeOperationResult extends RESTApiResult {
    private long filesCount;
    
    private SizeOperationResult(int type, long filesCount) {
        this.type = type;
        this.filesCount = filesCount;
    }
    
    public long getFilesCount() {
        return filesCount;
    }
    
    public static final SizeOperationResult build(int type, long filesCount) {
        return new SizeOperationResult(type, filesCount);
    }
}
