package ro.iordache.filestorage.repository.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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

    public List<String> enumerate(String pattern, long startIndex, long pageSize) {
        return scanRepo(pattern, startIndex, pageSize);
    }

    public boolean storeFile(String fileName, InputStream contentsInputStream) throws IOException {
        File destinationFile = storageHelper.getStorageFile(fileName);
        boolean isNew = false;
        logger.debug("Storing file {} in the internal storage", fileName);
        if (!destinationFile.exists()) {
            isNew = true;
        }
        destinationFile.getParentFile().mkdirs();
        FileCopyUtils.copy(contentsInputStream, 
                Files.newOutputStream(Paths.get(destinationFile.getAbsolutePath())));
        
        if(isNew) {
            size.incrementAndGet();
        }
        
        return isNew;
    }
    
    public boolean deleteFile(String fileName) throws IOException {
        logger.debug("Deleting file {} from the internal storage");
        File resolvedFileToDelete = storageHelper.findFile(fileName);
        
        if (resolvedFileToDelete == null) {
            logger.debug("[DELETE] No file found with name {}", fileName);
            return false;
        }
        
        Files.delete(Paths.get(resolvedFileToDelete.getAbsolutePath()));
        
        logger.debug("[DELETE] Deleting {} successful", fileName);
        
        size.decrementAndGet();
        return true;
    }
    
    public InputStream getFileContent(String fileName) throws IOException {
        logger.debug("Getting file contents for file {}", fileName);
        
        File resolvedFileToRead = storageHelper.findFile(fileName);
        if (resolvedFileToRead == null) {
            logger.debug("No file found with name {}", fileName);
            return null;
        }
        
        return new FileInputStream(resolvedFileToRead);
    }
    
    private List<String> scanRepo(String pattern, long startIndex, long pageSize) {
        logger.debug("Scanning file repository for pattern {}", pattern);
        long startScan = System.currentTimeMillis();
        List<String> results = null;

        results = storageIndex.scanRepoIndex(pattern, startIndex, pageSize);
        
        logger.debug("Scanning file repository for pattern {} took {} seconds", pattern, (long)(System.currentTimeMillis() - startScan)/100);
        
        return results;
    }
}
