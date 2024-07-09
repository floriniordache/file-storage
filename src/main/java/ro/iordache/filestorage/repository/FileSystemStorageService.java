package ro.iordache.filestorage.repository;

import java.io.IOException;
import java.io.InputStream;
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
     * Returns a list of file names matching a given pattern
     * 
     * @param pattern - a {@link String} denoting the glob pattern to match
     * @param startIndex - starting index in the result set
     * @param pageSize - max number of items per page
     * 
     * @return a {@link List} of file names matching the pattern
     */
    public List<String> enumerate(String pattern, long startIndex, long pageSize);
    
    /**
     * Stores a file in the server's storage 
     * 
     * @param fileName - the file name
     * @param contentsInputStream - an {@link InputStream} with the file's contents
     * @return - true if this is a new file or false otherwise
     * @throws IOException - on any IO issues while creating or updating the file
     */
    public boolean storeFile(String fileName, InputStream contentsInputStream) throws IOException;
    
    /**
     * Deletes a file from the server's storage
     * 
     * @param fileName - the file name to delete
     * @return true on success, false if the file could not be found in the storage
     * @throws IOException - on any IO issues during the operation
     */
    public boolean deleteFile(String fileName) throws IOException;
}
