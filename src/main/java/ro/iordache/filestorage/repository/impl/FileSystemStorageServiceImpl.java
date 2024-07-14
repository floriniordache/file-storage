package ro.iordache.filestorage.repository.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import jakarta.annotation.PostConstruct;
import ro.iordache.filestorage.repository.FileSystemStorageService;
import ro.iordache.filestorage.repository.impl.index.StorageIndex;
import ro.iordache.filestorage.repository.util.FileSystemStorageHelperImpl;

@Component
public class FileSystemStorageServiceImpl implements FileSystemStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageServiceImpl.class);
    
    // atomic long keeping track of the storage size
    private AtomicLong size;
    
    // map used to sync certain file operations 
    private ConcurrentHashMap<String, WeakReference<Object>> fileLocks;
    
    @Autowired
    private FileSystemStorageHelperImpl storageHelper;
    
    @Autowired
    private StorageIndex storageIndex;
    
    public FileSystemStorageServiceImpl() {
        size = new AtomicLong();
        
        fileLocks = new ConcurrentHashMap<String, WeakReference<Object>>();
    }
    
    @PostConstruct
    public void init() {
        long currentRepoSize = storageIndex.buildIndex(storageHelper.getStoragePath());
        size.set(currentRepoSize);
    }
    
    public long getSize() {
        return size.get();
    }

    /**
     * Searches the stored file names for a given {@link Pattern}
     * 
     * @param regexPattern - the {@link Pattern} to match
     * @param startIndex - starting index of the result set
     * @param pageSize - max number of items returned
     * @return a {@link List} of file names matching the pattern and the start/size parameters
     * 
     */
    public List<String> enumerate(Pattern regexPattern, long startIndex, long pageSize) {
        return scanRepo(regexPattern, startIndex, pageSize);
    }

    /**
     * Stores a file in the storage
     * 
     * @param fileName - the name of the file 
     * @param contentsInputStream - an {@link InputStream} with the file contents
     * 
     * @return a boolean flag indicating if the stored file is a new file or not
     */
    public boolean storeFile(String fileName, InputStream contentsInputStream) throws Exception {
        Path destinationFile = storageHelper.getStorageFile(fileName);
        boolean isNew = false;
        logger.debug("Storing file {} in the internal storage", fileName);

        Path tmpFile = null;
        try {
            // store the contents in a temporary file
            tmpFile = Files.createTempFile(storageHelper.getTempStoragePath(), null, ".tmp");
            
            FileCopyUtils.copy(contentsInputStream, 
                    Files.newOutputStream(tmpFile));
            
            // lock operation for this particular file
            Object fileLock = getOrCreateFileLock(fileName);
            synchronized(fileLock) {
                if (!Files.exists(destinationFile)) {
                    isNew = true;
                }
                //atomically move the temporary file at it's right location in the storage
                Files.move(tmpFile, destinationFile, StandardCopyOption.ATOMIC_MOVE);
                
                if(isNew) {
                    // increment repo size
                    size.incrementAndGet();
                    
                    // update index, add new file
                    storageIndex.addToIndex(fileName);
                }
            }
            
        } catch (Exception e) {
            logger.error("Something went wrong while transferring the content input stream to destination file!", e);
            
            // cleanup the temp file if it's already there
            if (tmpFile != null && Files.exists(tmpFile)) {
                try {
                    Files.delete(tmpFile);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            
            // re-throw the exception, operation was not successful
            throw e;
        }
        
        return isNew;
    }
    
    public boolean deleteFile(String fileName) throws IOException {
        logger.debug("Deleting file {} from the internal storage", fileName);
        Path resolvedFileToDelete = storageHelper.findFile(fileName);
        
        if (resolvedFileToDelete == null) {
            logger.debug("[DELETE] No file found with name {}", fileName);
            return false;
        }
        
        // lock operation for this particular file
        Object fileLock = getOrCreateFileLock(fileName);
        synchronized(fileLock) {
            Files.delete(resolvedFileToDelete);
            
            // update the store size and index
            size.decrementAndGet();
            storageIndex.removeFromIndex(fileName);
        }
        
        logger.debug("[DELETE] Deleting {} successful", fileName);
        return true;
    }
    
    public InputStream getFileContent(String fileName) throws IOException {
        logger.debug("Getting file contents for file {}", fileName);
        
        Path resolvedFileToRead = storageHelper.findFile(fileName);
        if (resolvedFileToRead == null) {
            logger.debug("No file found with name {}", fileName);
            return null;
        }
        
        return Files.newInputStream(resolvedFileToRead);
    }
    
    /**
     * Gets or creates an object to be used to synchronize operations on a file
     * 
     * @param fileName the file name
     * @return an {@link Object} to be used in synchronizing operations on the given file name
     */
    private synchronized Object getOrCreateFileLock(String fileName) {
        WeakReference<Object> fileLockReference = fileLocks.get(fileName);
        Object fileLock = null;
        
        if (fileLockReference != null) {
            fileLock = fileLockReference.get();
        }
        
        if (fileLock == null) {
            fileLock = new Object();
            fileLocks.put(fileName, new WeakReference<Object>(fileLock));
        }
        
        return fileLock;
    }
    
    private List<String> scanRepo(Pattern regexPattern, long startIndex, long pageSize) {
        logger.debug("Scanning file repository for pattern {}", regexPattern.toString());
        long startScan = System.currentTimeMillis();
        List<String> results = null;

        results = storageIndex.scanRepoIndex(regexPattern, startIndex, pageSize);
        
        logger.debug("Scanning file repository for pattern {} took {} seconds", regexPattern.toString(), (long)(System.currentTimeMillis() - startScan)/100);
        
        return results;
    }
}
