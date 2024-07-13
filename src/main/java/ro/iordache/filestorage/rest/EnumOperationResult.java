package ro.iordache.filestorage.rest;

import java.util.List;

/**
 * Enum files operation result
 */
public class EnumOperationResult extends RESTApiResult {
    
    private long startIndex;
    private int itemCount;
    private List<String> matchingItems;
    
    private EnumOperationResult(int type, long startIndex, List<String> matchingItems) {
        this.type = type;
        this.startIndex = startIndex;
        this.itemCount = matchingItems.size();
        this.matchingItems = matchingItems;
    }
    
    public long getStartIndex() {
        return this.startIndex;
    }
    
    public int getItemCount() {
        return this.itemCount;
    }
    
    public List<String> getMatchingItems() {
        return this.matchingItems;
    }
    
    public static final EnumOperationResult build(int type, long startIndex, List<String> matchingItems) {
        return new EnumOperationResult(type, startIndex, matchingItems);
    }
}
