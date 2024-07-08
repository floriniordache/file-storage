package ro.iordache.filestorage.repository;

import java.util.List;

public interface FileSystemStorageService {
    
    /**
     * Gets the total size of this store
     * 
     * @return number of files in the file store
     *  
     */
    public long getSize();
    
    /**
     * Increments the count size of the repository by 1
     */
    public void incrementSize();
    
    /**
     * Decrements the count size of the repository by 1
     */
    public void decrementSize();
    
    /**
     * Returns a list of file names matching a given pattern
     * 
     * @param pattern - a {@link String} denoting the glob pattern to match
     * @param startIndex - starting index in the result set
     * @param pageSize - max number of items per page
     * 
     * @return a {@link List} of file names matching the pattern
     */
    public List<String> enumerate(String pattern, long startIndex, long pageSize);
}
