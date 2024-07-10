package ro.iordache.filestorage.repository.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
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
    
    private AtomicLong size;
    
    @Autowired
    private FileSystemStorageHelperImpl storageHelper;
    
    @Autowired
    private StorageIndex storageIndex;
    
    public FileSystemStorageServiceImpl() {
        size = new AtomicLong();
    }
    
    @PostConstruct
    public void init() {
        long currentRepoSize = storageIndex.buildIndex(storageHelper.getStoragePath());
        size.set(currentRepoSize);
    }
    
    public long getSize() {
        return size.get();
    }

    public List<String> enumerate(Pattern regexPattern, long startIndex, long pageSize) {
        return scanRepo(regexPattern, startIndex, pageSize);
    }

    public boolean storeFile(String fileName, InputStream contentsInputStream) {
        Path destinationFile = storageHelper.getStorageFile(fileName);
        boolean isNew = false;
        logger.debug("Storing file {} in the internal storage", fileName);
        if (!Files.exists(destinationFile)) {
            isNew = true;
        }
        

        // store a temp file with the contents
        Path tmpFile = null;
        try {
            tmpFile = Files.createTempFile(storageHelper.getTempStoragePath(), null, ".tmp");
            
            FileCopyUtils.copy(contentsInputStream, 
                    Files.newOutputStream(tmpFile));
            
            //atomically move the temporary file at it's right location in the storage
            Files.move(tmpFile, destinationFile, StandardCopyOption.ATOMIC_MOVE);
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
        }
        
        if(isNew) {
            // increment repo size
            size.incrementAndGet();
            
            // update index, add new file
            storageIndex.addToIndex(fileName);
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
        
        Files.delete(resolvedFileToDelete);
        
        logger.debug("[DELETE] Deleting {} successful", fileName);
        
        // update the store size and index
        size.decrementAndGet();
        storageIndex.removeFromIndex(fileName);
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
    
    private List<String> scanRepo(Pattern regexPattern, long startIndex, long pageSize) {
        logger.debug("Scanning file repository for pattern {}", regexPattern.toString());
        long startScan = System.currentTimeMillis();
        List<String> results = null;

        results = storageIndex.scanRepoIndex(regexPattern, startIndex, pageSize);
        
        logger.debug("Scanning file repository for pattern {} took {} seconds", regexPattern.toString(), (long)(System.currentTimeMillis() - startScan)/100);
        
        return results;
    }
}
