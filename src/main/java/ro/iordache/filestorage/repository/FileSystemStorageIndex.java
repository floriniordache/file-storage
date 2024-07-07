package ro.iordache.filestorage.repository;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ro.iordache.filestorage.repository.util.FileSystemStorageHelperImpl;

@Component
public class FileSystemStorageIndex {
    
    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageIndex.class);
    
    private static final String INDEX_CACHE_FILE_NAME = "storageIndex.cache";
    
    private AtomicLong size;
    
    @Autowired
    private FileSystemStorageHelperImpl storageHelper;
    
    public FileSystemStorageIndex() {
        size = new AtomicLong(0);
    }
    
    public long getSize() {
        return size.get();
    }
    
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void buildIndex() {
        logger.debug("File system storage index rebuilding...");
        
        long repoSize = 0;
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        
        try {
            DirectoryStream<Path> ds = Files.newDirectoryStream(storageHelper.getStoragePath());
            FileChannel cachedIndexFC = FileChannel.open(storageHelper.getTempFilePath(INDEX_CACHE_FILE_NAME), 
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            
            StringBuilder sb = new StringBuilder();
            
            for (Path storedFilePath : ds) {
                repoSize++;

                sb.append(storedFilePath.getFileName().toString());
                sb.append("\n");

                if (sb.length() > 4096) {
                    buffer = ByteBuffer.wrap(sb.toString().getBytes());
                    
                    cachedIndexFC.write(buffer);
                    
                    sb.setLength(0);
                }
            }
            
            ds.close();
            
            size.set(repoSize);
        } catch (IOException e) {
            logger.error("Error rebuilding index!", e);
        }
        
        logger.debug("File system storage index rebuild finished");
    }
}
