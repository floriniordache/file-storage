package ro.iordache.filestorage.repository.impl.index;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread to handle batch updates to to FileInfoEntry index files when new file names need to be added to index
 */
public class BatchNewFilesIndexUpdaterThread extends Thread {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchNewFilesIndexUpdaterThread.class);
    
    private BlockingQueue<String> newFilesBlockingQueue;
    
    private String indexFileName;
    
    public BatchNewFilesIndexUpdaterThread(BlockingQueue<String> queue, String indexFileName) {
        this.newFilesBlockingQueue = queue;
        
        this.indexFileName = indexFileName;
    }
    
    public void run(){
        try {
            String fileName;
            while((fileName = newFilesBlockingQueue.take()) != null) {
                List<String> allNewFiles = new ArrayList<String>();
                newFilesBlockingQueue.drainTo(allNewFiles);
                allNewFiles.add(fileName);
                
                logger.debug("Batch adding {} files to the index...", allNewFiles.size());
                try {
                    FileChannel indexFileChannel = FileChannel.open(Paths.get(this.indexFileName), 
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
}
