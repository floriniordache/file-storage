package ro.iordache.filestorage.repository.impl.index;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles storage index operations
 */
@Component
public class StorageIndex {
    private static final Logger logger = LoggerFactory.getLogger(StorageIndex.class);
    
    public static final String STORAGE_INDEX_FILE_NAME = "storage.index";
    
    public long buildIndex(Path folder) {
        logger.debug("Building storage index file");
        long numEntries = 0;
        
        try {
            DirectoryStream<Path> ds = Files.newDirectoryStream(folder);
            
            FileChannel indexFileChannel = FileChannel.open(Paths.get(STORAGE_INDEX_FILE_NAME), 
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            ByteBuffer buf = ByteBuffer.allocate(5000 * FileInfoIndexEntry.MAX_RECORD_LENGTH);
            
            for (Path filePath : ds) {
                FileInfoIndexEntry indexEntry = new FileInfoIndexEntry(filePath.getFileName().toString());
                byte[] indexEntryBytes = indexEntry.getBytes();
                
                if (buf.position() + indexEntryBytes.length > buf.capacity()) {
                    buf.flip();
                    
                    indexFileChannel.write(buf);
                    buf.clear();
                }
                
                buf.put(indexEntryBytes);
                
                numEntries++;
            }
            
            if (buf.position() > 0) {
                buf.flip();
                indexFileChannel.write(buf);
            }
            
            ds.close();
            
            indexFileChannel.force(false);
            indexFileChannel.close();
        } catch (IOException e) {
            logger.error("Error indexing repository storage!", e);
        }
        
        logger.debug("Building storage index file done!");
        return numEntries;
    }
    
    public List<String> scanRepoIndex(String pattern, long startIdx, long pageSize) {
        List<String> hits = new ArrayList<String>();        
        FileChannel indexFileChannel = null;
        
        try {
            Pattern regexPattern = Pattern.compile(pattern); 
            
            indexFileChannel = FileChannel.open(Paths.get(STORAGE_INDEX_FILE_NAME), StandardOpenOption.READ);
            
            ByteBuffer buffer = ByteBuffer.allocate(5000 * FileInfoIndexEntry.MAX_RECORD_LENGTH);
            long readBytes;
            
            long skipRecords = startIdx;
            while ((readBytes = indexFileChannel.read(buffer)) >= 0) {
                buffer.flip();
                List<FileInfoIndexEntry> readEntries = FileInfoIndexEntry.fromByteArray(buffer.array());
                
                for (FileInfoIndexEntry fileInfoIndexEntry : readEntries) {
                    Matcher regexMatcher = regexPattern.matcher(fileInfoIndexEntry.getFileName());
                    if (regexMatcher.matches()) {
                        if (skipRecords > 0) {
                            skipRecords--;
                            continue;
                        } else {
                            hits.add(fileInfoIndexEntry.getFileName());
                            
                            if (hits.size() >= pageSize) {
                                return hits;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error scanning file repository for pattern!", e);
        } finally {
            if (indexFileChannel != null) {
                try {
                    indexFileChannel.close();
                } catch (Exception e) {}
            }
        }
        
        return hits;
    }
}
