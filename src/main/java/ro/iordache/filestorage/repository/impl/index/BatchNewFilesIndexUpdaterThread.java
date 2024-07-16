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
                    FileChannel indexFileChannel = FileChannel.open(Paths.get(indexFileName), 
                            StandardOpenOption.READ, StandardOpenOption.WRITE);

                    ByteBuffer buffer = ByteBuffer.allocate(5000 * FileInfoIndexEntry.MAX_RECORD_LENGTH);
                    long readBytes;
                    
                    while ((readBytes = indexFileChannel.read(buffer)) >= 0) {
                        buffer.flip();
                        
                        List<FileInfoIndexEntry> readEntries = FileInfoIndexEntry.fromByteArray(buffer);
                        
                        for (int i = 0 ; i < readEntries.size() ; i++) {
                            FileInfoIndexEntry fileInfoIndexEntry = readEntries.get(i);
                            
                            if (fileInfoIndexEntry.getFileName().isEmpty()) {
                                String fileToAdd = allNewFiles.get(0);
                                logger.trace("Found empty space in index file, adding file {} ", fileToAdd);
                                
                                // need to add over this empty area in the index
                                long currentFileCursorPos = indexFileChannel.position();
                                
                                // we need to update the entry at the right position in the file
                                long updatePosition = currentFileCursorPos - (readEntries.size() - i)*FileInfoIndexEntry.MAX_RECORD_LENGTH;
                                
                                // update file channel position
                                indexFileChannel.position(updatePosition);
                                
                                // overwrite the empty area with the name that needs to be added
                                ByteBuffer whiteSpaces = ByteBuffer.wrap(new FileInfoIndexEntry(fileToAdd).getBytes());
                                indexFileChannel.write(whiteSpaces);
                                
                                // update position to initial value
                                indexFileChannel.position(currentFileCursorPos);
                                
                                logger.trace("Adding file {} to index done!", fileToAdd);
                                allNewFiles.remove(0);
                                
                                if (allNewFiles.size() == 0) {
                                    break;
                                }
                            }
                        }
                        
                        if (allNewFiles.size() == 0) {
                            break;
                        }
                    }
                    
                    // check if there are more files to add, they need to be appended
                    if (allNewFiles.size() > 0) {
                        // make sure we're at the end
                        indexFileChannel.position(indexFileChannel.size());
                        
                        ByteBuffer buf = ByteBuffer.allocate(allNewFiles.size() * FileInfoIndexEntry.MAX_RECORD_LENGTH);
                        for (String newFileName : allNewFiles) {
                            FileInfoIndexEntry indexEntry = new FileInfoIndexEntry(newFileName);
                            byte[] indexEntryBytes = indexEntry.getBytes();
                            buf.put(indexEntryBytes);
                        }
                        
                        buf.flip();
                        indexFileChannel.write(buf);
                    }
                    
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
