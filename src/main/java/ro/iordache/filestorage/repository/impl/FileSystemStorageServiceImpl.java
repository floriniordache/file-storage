package ro.iordache.filestorage.repository.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import jakarta.annotation.PostConstruct;
import ro.iordache.filestorage.repository.FileSystemStorageService;
import ro.iordache.filestorage.repository.util.FileSystemStorageHelperImpl;

@Component
public class FileSystemStorageServiceImpl implements FileSystemStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageServiceImpl.class);
    
    private AtomicLong size;
    
    @Autowired
    private FileSystemStorageHelperImpl storageHelper;
    
    public FileSystemStorageServiceImpl() {
        size = new AtomicLong();
    }
    
    @PostConstruct
    public void init() {
        size.set(scanRepoSize());
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
    
    private long scanRepoSize() {
        logger.debug("Determining size of the repository...");
        
        long repoSize = 0;
        try {
            DirectoryStream<Path> ds = Files.newDirectoryStream(storageHelper.getStoragePath(), "*");
            for (Path storedFilePath : ds) {
                repoSize++;
            }
            ds.close();
            
            logger.debug("Found total {} files in the storage!", repoSize);
        } catch (IOException e) {
            logger.error("Error scanning file repository for pattern!", e);
        }
        
        return repoSize;
    }
    
    private List<String> scanRepo(String globPattern, long startIndex, long pageSize) {
        logger.debug("Scanning file repository for pattern {...}", globPattern);
        List<String> results = new ArrayList<String>();

        try {
            DirectoryStream<Path> ds = Files.newDirectoryStream(storageHelper.getStoragePath(), globPattern);
            long current = 0;
            
            for (Path storedFilePath : ds) {
                if (current < startIndex) {
                    current++;
                    continue;
                } else if (current >= startIndex && current < startIndex + pageSize) {
                    current++;
                    results.add(storedFilePath.getFileName().toString());
                } else {
                    break;
                }
            }
            ds.close();
        } catch (IOException e) {
            logger.error("Error scanning file repository for pattern!", e);
        }
        
        logger.debug("Scanning file repository for pattern {} finished!", globPattern);
        
        return results;
    }
    
    /*
     * public void buildIndex() {
        logger.debug("File system storage index rebuilding...");
        
        long repoSize = 0;
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        
        try {
            DirectoryStream<Path> ds = Files.newDirectoryStream(storageHelper.getStoragePath());
            FileChannel cachedIndexFC = FileChannel.open(storageHelper.getTempFilePath(INDEX_CACHE_FILE_NAME), 
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            
            for (Path storedFilePath : ds) {
                repoSize++;

                buffer.put(storedFilePath.getFileName().toString().getBytes());
                buffer.put("\n".getBytes());
                buffer.flip();
                
                cachedIndexFC.write(buffer);
                
                buffer.clear();
            }
            
            cachedIndexFC.force(false);
            cachedIndexFC.close();
            ds.close();
            
            size.set(repoSize);
        } catch (IOException e) {
            logger.error("Error rebuilding index!", e);
        }
        
        logger.debug("File system storage index rebuild finished");
    }
     */
}
