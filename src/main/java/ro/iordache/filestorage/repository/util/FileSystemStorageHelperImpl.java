package ro.iordache.filestorage.repository.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileSystemStorageHelperImpl {
    
    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageHelperImpl.class);
    
    private String storageRepositoryFolder;
    
    private String tempFolderName;
    
    public FileSystemStorageHelperImpl(@Value("${filestorage.repo.folder:storage}") String storageRepositoryFolder,
            @Value("${filestorage.repo.temp.name:temp}") String tempFolderName) {
        this.storageRepositoryFolder = storageRepositoryFolder;
        this.tempFolderName = tempFolderName;

        try {
            createFolder(tempFolderName);
            createFolder(storageRepositoryFolder);
        } catch (IOException e) {
            logger.error("Could not create folders!", e);
        }
    }
    
    private void createFolder(String folderPathStr) throws IOException {
        Path folderPath = Paths.get(folderPathStr);
        
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }
    }
    
    public Path getStoragePath() {
        return Paths.get(storageRepositoryFolder);
    }
    
    public Path getTempStoragePath() {
        return Paths.get(tempFolderName);
    }
    
    /**
     * Gets the internal storage file associated with a given file name
     * 
     * @param fileName - name of the file looked up in the storage
     * @return a {@link Path} object in the internal storage repository
     */
    public Path getStorageFile(String fileName) {
        Path resolvedFile = Paths.get(storageRepositoryFolder, fileName);
        
        return resolvedFile;
    }
    
    /**
     * Attempts to resolve a file in the storage with a given file name
     * 
     * @param fileName the name of the file we want in the storage
     * @return an actual {@link Path} in this storage's repository folder, if found, null otherwise
     */
    public Path findFile(String fileName) {
        
        logger.debug("Trying to resolve a file with name {} in the storage folder {}", fileName, storageRepositoryFolder);
        Path resolvedFile = getStorageFile(fileName);
        
        if (!Files.exists(resolvedFile) || Files.isDirectory(resolvedFile)) {
            logger.debug("No file found with name {} in the storage folder {}", fileName, storageRepositoryFolder);
            return null;
        }
        
        logger.debug("Successfully resolved file with name {} in the storage folder {}", fileName, storageRepositoryFolder);
        return resolvedFile;
    }
}
