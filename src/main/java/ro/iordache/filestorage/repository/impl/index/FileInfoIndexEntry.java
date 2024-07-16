package ro.iordache.filestorage.repository.impl.index;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Index entry in the storage index file
 */
public class FileInfoIndexEntry {

    /**
     * Max filename length
     */
    public static final int MAX_RECORD_LENGTH = 64;
    
    private String fileName;
    
    private byte[] bytes;
    
    public FileInfoIndexEntry(String fileName) {
        this.fileName = fileName;
        
        StringBuilder sb = new StringBuilder();
        sb.append(fileName);
        
        // pad with empty spaces so byte representation is exactly MAX_RECORD_LENGTH
        while (sb.length() < MAX_RECORD_LENGTH) {
            sb.append(" ");
        }
        
        this.bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    public FileInfoIndexEntry(byte[] bytes) {
        this.fileName = new String(bytes, StandardCharsets.UTF_8).trim();
        this.bytes = bytes;
    }
    
    public String getFileName() {
        return this.fileName;
    }
    
    public byte[] getBytes() {
        return this.bytes;
    }

    /**
     * Build a list of {@link FileInfoIndexEntry} out of a byte buffer
     * 
     * @param byteBuf - the {@link ByteBuffer}
     * @return a {@link List} of {@link FileInfoIndexEntry} items
     */
    public static List<FileInfoIndexEntry> fromByteArray(ByteBuffer byteBuf) {
        
        List<FileInfoIndexEntry> resList = new ArrayList<FileInfoIndexEntry>();
        byte[] bytes = byteBuf.array();
        
        int position = 0;
        
        while (position < byteBuf.limit()) {
            byte[] fileEntryBytes = Arrays.copyOfRange(bytes, position, position + MAX_RECORD_LENGTH);
            
            FileInfoIndexEntry entry = new FileInfoIndexEntry(fileEntryBytes);
            resList.add(entry);
            
            position += MAX_RECORD_LENGTH;
        }
        
        return resList;
    }
}
