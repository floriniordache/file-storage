package ro.iordache.filestorage.rest;

import java.util.regex.Pattern;

/**
 * Interface for repository size and enumeration operations
 */
public interface EnumServiceHandler {
    
    /**
     * Returns the number of files in this repository
     * 
     * @return a {@link SizeOperationResult} containing a count of the number of files stored
     */
    public SizeOperationResult getRepositorySize();
    
    /**
     * Enumerates the files in the storage based on a pattern
     * 
     * @param regexPattern - a {@link Pattern} of the regex pattern to match file names
     * @param startIndex - start index of the result set
     * @param pageSize - max number of items in the results set
     * @return an {@link EnumOperationResult} object containing the results
     */
    public EnumOperationResult enumerate(Pattern regexPattern, long startIndex, long pageSize);
}