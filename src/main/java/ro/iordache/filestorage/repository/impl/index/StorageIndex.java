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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
    
    public StorageIndex() {
        newFilesBlockingQueue = new ArrayBlockingQueue<String>(1000);
        
        (new Thread(new Runnable(){
            public void run(){
                try {
                    String fileName;
                    while((fileName = newFilesBlockingQueue.take()) != null) {
                        List<String> allNewFiles = new ArrayList<String>();
                        newFilesBlockingQueue.drainTo(allNewFiles);
                        allNewFiles.add(fileName);
                        
                        logger.debug("Batch adding {} files to the index...", allNewFiles.size());
                        try {
                            FileChannel indexFileChannel = FileChannel.open(Paths.get(STORAGE_INDEX_FILE_NAME), 
                                    StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                            
                            ByteBuffer buf = ByteBuffer.allocate(allNewFiles.size() * FileInfoIndexEntry.MAX_RECORD_LENGTH);

                            for (String newFileName : allNewFiles) {
                                FileInfoIndexEntry indexEntry = new FileInfoIndexEntry(newFileName);
                                byte[] indexEntryBytes = indexEntry.getBytes();
                                buf.put(indexEntryBytes);
                            }
                            
                            buf.flip();
                            indexFileChannel.write(buf);
                            indexFileChannel.force(false);
                            indexFileChannel.close();
                            logger.debug("Batch adding {} files to the index successful!", allNewFiles.size());
                        } catch (Exception e) {
                            logger.error("Error batch updating index!", e);
                        }
                        
                    }
                } catch (Exception e) {
                    logger.error("Async batching updates to index storage failed!", e);
                }

            }
         })).start();
    }
    
    public long buildIndex(Path folder) {
        logger.debug("Building storage index file");
        long numEntries = 0;
        
        try {
            DirectoryStream<Path> ds = Files.newDirectoryStream(folder);
            
            FileChannel indexFileChannel = FileChannel.open(Paths.get(STORAGE_INDEX_FILE_NAME), 
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
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
    
    public void removeFromIndex(String fileName) {
        logger.debug("Removing file {} from the index...", fileName);
        try {
            FileChannel indexFileChannel = FileChannel.open(Paths.get(STORAGE_INDEX_FILE_NAME), 
                    StandardOpenOption.READ, StandardOpenOption.WRITE);

            ByteBuffer buffer = ByteBuffer.allocate(5000 * FileInfoIndexEntry.MAX_RECORD_LENGTH);
            long readBytes;
            
            while ((readBytes = indexFileChannel.read(buffer)) >= 0) {
                buffer.flip();
                
                List<FileInfoIndexEntry> readEntries = FileInfoIndexEntry.fromByteArray(buffer);
                
                for (int i = 0 ; i < readEntries.size() ; i++) {
                    FileInfoIndexEntry fileInfoIndexEntry = readEntries.get(i);

                    if (fileInfoIndexEntry.getFileName().equals(fileName)) {
                        logger.debug("Found file {} in the index storage, removing it...", fileName);
                        
                        // need to remove this entry from the index file
                        long currentFileCursorPos = indexFileChannel.position();
                        
                        // we need to "erase" the entry at the right position in the file
                        long updatePosition = currentFileCursorPos - (readEntries.size() - i)*FileInfoIndexEntry.MAX_RECORD_LENGTH;
                        
                        // update file channel position
                        indexFileChannel.position(updatePosition);
                        
                        // overwrite the area for the filename that needs to be removed
                        ByteBuffer whiteSpaces = ByteBuffer.wrap(new FileInfoIndexEntry("").getBytes());
                        indexFileChannel.write(whiteSpaces);
                        
                        indexFileChannel.force(false);
                        indexFileChannel.close();
                        
                        logger.debug("Removing file {} from index done!", fileName);
                        
                        return;
                        
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error removing file from index!", e);
        }
    }
    
    public void addToIndex(String fileName) {
        
        // queue the file name to be (eventually) written to the index
        newFilesBlockingQueue.add(fileName);
    }
    
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
