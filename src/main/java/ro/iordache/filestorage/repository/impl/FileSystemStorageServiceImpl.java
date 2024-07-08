package ro.iordache.filestorage.repository.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        scanRepo("*");
    }
    
    public long getSize() {
        return size.get();
    }

    public void incrementSize() {
        size.incrementAndGet();
    }

    public void decrementSize() {
        size.decrementAndGet();
    }
    

    public List<String> enumerate(String pattern, long startIndex, long pageSize) {
        // TODO Auto-generated method stub
        return null;
    }

    
    public void scanRepo(String globPattern) {
        logger.debug("Scanning file repository for pattern {...}", globPattern);
        
        long repoSize = 0;
        try {
            DirectoryStream<Path> ds = Files.newDirectoryStream(storageHelper.getStoragePath(), globPattern);
            for (Path storedFilePath : ds) {
                repoSize++;
            }

            ds.close();
            
            size.set(repoSize);
            
            logger.debug("Found total {} files for pattern {}!", repoSize, globPattern);
        } catch (IOException e) {
            logger.error("Error scanning file repository for pattern!", e);
        }
        
        logger.debug("Scanning file repository for pattern {} finished!", globPattern);
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
