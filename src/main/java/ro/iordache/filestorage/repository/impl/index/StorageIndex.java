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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
    
    /**
     * queue to batch newly added file updates to the index
     */
    private BlockingQueue<String> newFilesBlockingQueue;
    
    
    /**
     * Queue to batch removed files to the index file
     */
    private BlockingQueue<String> deletedFilesBlockingQueue;
    
    public StorageIndex() {
        newFilesBlockingQueue = new LinkedBlockingQueue<String>();
        deletedFilesBlockingQueue = new LinkedBlockingQueue<String>();
        
        logger.debug("Starting thread process to batch update new files to the index...");
        (new BatchNewFilesIndexUpdaterThread(newFilesBlockingQueue, STORAGE_INDEX_FILE_NAME)).start();
        
        logger.debug("Starting thread process to batch update deleted files to the index...");
        (new BatchDeletedFilesIndexUpdaterThread(deletedFilesBlockingQueue, STORAGE_INDEX_FILE_NAME)).start();
    }
    
    /**
     * Scans all files in the storage folder and adds the names to the index file for quicker scanning
     * 
     * @param folder - the folder to scan
     * @return - number of found file entries
     */
    public long buildIndex(Path folder) {
        logger.debug("Building storage index file");
        long startIndexBuild = System.currentTimeMillis();
        long numEntries = 0;
        
        try {
            DirectoryStream<Path> ds = Files.newDirectoryStream(folder);
            
            FileChannel indexFileChannel = FileChannel.open(Paths.get(STORAGE_INDEX_FILE_NAME), 
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            ByteBuffer buf = ByteBuffer.allocate(10000 * FileInfoIndexEntry.MAX_RECORD_LENGTH);
            
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
        
        logger.debug("Building storage index file done in {} seconds!", (float)(System.currentTimeMillis() - startIndexBuild)/1000);
        return numEntries;
    }
    
    public void removeFromIndex(String fileName) {
        // queue the filename to be (eventually) erased from the index
        deletedFilesBlockingQueue.add(fileName);
    }
    
    public void addToIndex(String fileName) {
        // queue the file name to be (eventually) written to the index
        newFilesBlockingQueue.add(fileName);
    }
    
    /**
     * Scans the index file and looks up a given regex {@link Pattern}
     * Results will be paged by leveraging a start index and a page size to avoid returning a large result set
     * 
     * @param regexPattern the regex {@link Pattern}
     * @param startIdx - starting index of the result set
     * @param pageSize - max number of items in the result set
     * @return - a {@link List} with matching entries
     */
    public List<String> scanRepoIndex(Pattern regexPattern, long startIdx, long pageSize) {
        List<String> hits = new ArrayList<String>();
        FileChannel indexFileChannel = null;
        
        try {
            indexFileChannel = FileChannel.open(Paths.get(STORAGE_INDEX_FILE_NAME), StandardOpenOption.READ);
            
            ByteBuffer buffer = ByteBuffer.allocate(5000 * FileInfoIndexEntry.MAX_RECORD_LENGTH);
            long readBytes;
            
            long skipRecords = startIdx;
            while ((readBytes = indexFileChannel.read(buffer)) >= 0) {
                buffer.flip();
                List<FileInfoIndexEntry> readEntries = FileInfoIndexEntry.fromByteArray(buffer);
                
                for (FileInfoIndexEntry fileInfoIndexEntry : readEntries) {
                    
                    if (fileInfoIndexEntry.getFileName().isEmpty()) {
                        continue;
                    }
                    
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
