package ro.iordache.filestorage.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

public interface FileSystemStorageService {
    
    /**
     * Gets the total size of this store
     * 
     * @return number of files in the file store
     *  
     */
    public long getSize();
    
    /**
     * Stores a file in the server's storage 
     * 
     * @param fileName - the file name
     * @param contentsInputStream - an {@link InputStream} with the file's contents
     * @return - true if this is a new file or false otherwise
     */
    public boolean storeFile(String fileName, InputStream contentsInputStream) throws Exception;
    
    /**
     * Deletes a file from the server's storage
     * 
     * @param fileName - the file name to delete
     * @return true on success, false if the file could not be found in the storage
     * @throws IOException - on any IO issues during the operation
     */
    public boolean deleteFile(String fileName) throws IOException;
    

    /**
     * Returns a stored file's contents as an {@link InputStream}
     * 
     * @param fileName - the file name
     * @return an {@link InputStream} from the file, if the file is found to be stored in this server's storage,
     * {@code null} otherwise
     * @throws IOException
     */
    public InputStream getFileContent(String fileName) throws IOException;
    
    /**
     * Returns a list of file names matching a given pattern
     * 
     * @param regexPattern - a compiled {@link Pattern} representing the regex to be matched
     * @param startIndex - starting index in the result set
     * @param pageSize - max number of items per page
     * 
     * @return a {@link List} of file names matching the pattern
     */
    public List<String> enumerate(Pattern regexPattern, long startIndex, long pageSize);
}
