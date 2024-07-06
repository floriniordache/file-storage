package ro.iordache.filestorage.rest.impl;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileSystemStorageHelperImpl {
    
    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageHelperImpl.class);
    
    @Value("${filestorage.repo.folder}")
    private String storageRepositoryFolder;
    
    /**
     * Gets the internal storage file associated with a given file name
     * 
     * @param fileName - name of the file looked up in the storage
     * @return a {@link File} object in the internal storage repository
     */
    public File getStorageFile(String fileName) {
        File resolvedFile = new File(storageRepositoryFolder, fileName);
        
        return resolvedFile;
    }
    
    /**
     * Attempts to resolve a file in the storage with a given file name
     * 
     * @param fileName the name of the file we want in the storage
     * @return an actual {@link File} in this storage's repository folder, if found, null otherwise
     */
    public File findFile(String fileName) {
        
        logger.debug("Trying to resolve a file with name {} in the storage folder {}", fileName, storageRepositoryFolder);
        File resolvedFile = getStorageFile(fileName);
        
        if (!resolvedFile.exists() || resolvedFile.isDirectory()) {
            logger.debug("No file found with name {} in the storage folder {}", fileName, storageRepositoryFolder);
            return null;
        }
        
        logger.debug("Successfully resolved file with name {} in the storage folder {}", fileName, storageRepositoryFolder);
        return resolvedFile;
    }
}
