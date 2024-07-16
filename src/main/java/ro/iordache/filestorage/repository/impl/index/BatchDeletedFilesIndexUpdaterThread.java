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
 * Thread to handle batch updates to to FileInfoEntry index files when file names need to be removed from the index
 */
public class BatchDeletedFilesIndexUpdaterThread extends Thread {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchDeletedFilesIndexUpdaterThread.class);
    
    private BlockingQueue<String> deletedFilesBlockingQueue;
    
    private String indexFileName;
    
    public BatchDeletedFilesIndexUpdaterThread(BlockingQueue<String> queue, String indexFileName) {
        this.deletedFilesBlockingQueue = queue;
        
        this.indexFileName = indexFileName;
    }
    
    public void run(){
        try {
            
            String firstFileName;
            while((firstFileName = deletedFilesBlockingQueue.take()) != null) {
                List<String> deletedFiles = new ArrayList<String>();
                deletedFilesBlockingQueue.drainTo(deletedFiles);
                deletedFiles.add(firstFileName);
                
                int filesBatchCount = deletedFiles.size();
                
                logger.debug("Batch removing {} files from the index...", filesBatchCount);
                
                FileChannel indexFileChannel = FileChannel.open(Paths.get(indexFileName), 
                        StandardOpenOption.READ, StandardOpenOption.WRITE);

                // do reads in 5k record chunks
                ByteBuffer buffer = ByteBuffer.allocate(5000 * FileInfoIndexEntry.MAX_RECORD_LENGTH);
                long readBytes;
                
                while ((readBytes = indexFileChannel.read(buffer)) >= 0) {
                    buffer.flip();
                    
                    List<FileInfoIndexEntry> readEntries = FileInfoIndexEntry.fromByteArray(buffer);
                    
                    for (int i = 0 ; i < readEntries.size() ; i++) {
                        FileInfoIndexEntry fileInfoIndexEntry = readEntries.get(i);
                        
                        if (fileInfoIndexEntry.getFileName().isEmpty()) {
                            continue;
                        }
                        
                        int deletedFileArrIdx = deletedFiles.indexOf(fileInfoIndexEntry.getFileName());

                        // if we have a match
                        if (deletedFileArrIdx >= 0) {
                            logger.trace("Found file {} in the index storage, removing it...", fileInfoIndexEntry.getFileName());
                            
                            // need to remove this entry from the index file
                            long currentFileCursorPos = indexFileChannel.position();
                            
                            // we need to "erase" the entry at the right position in the file
                            long updatePosition = currentFileCursorPos - (readEntries.size() - i)*FileInfoIndexEntry.MAX_RECORD_LENGTH;
                            
                            // update file channel position
                            indexFileChannel.position(updatePosition);
                            
                            // overwrite the area for the filename that needs to be removed
                            ByteBuffer whiteSpaces = ByteBuffer.wrap(new FileInfoIndexEntry("").getBytes());
                            indexFileChannel.write(whiteSpaces);
                            
                            // update position to initial value
                            indexFileChannel.position(currentFileCursorPos);
                            
                            // remove the deleted file from the array
                            deletedFiles.remove(deletedFileArrIdx);
                            
                            logger.trace("Removing file {} from index done!", fileInfoIndexEntry.getFileName());
                            
                            if (deletedFiles.size() == 0) {
                                // we're done erasing all files in the batch from the index
                                break;
                            }
                        }
                    }
                    
                    if (deletedFiles.size() == 0) {
                        // we're done erasing all files in the batch from the index
                        break;
                    }
                }
                
                indexFileChannel.force(false);
                indexFileChannel.close();
                
                logger.debug("Batch removing {} files from the index done!", filesBatchCount);
            }
        } catch (Exception e) {
            logger.error("Async batching updates to index storage failed!", e);
        }

    }
}
