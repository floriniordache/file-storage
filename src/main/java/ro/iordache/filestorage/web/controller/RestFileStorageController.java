package ro.iordache.filestorage.web.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ro.iordache.filestorage.rest.impl.ReadFileServiceHandler;

@RestController
@RequestMapping("/api/v1/files")
public class RestFileStorageController {
    
    @Value("${filestorage.repo.folder}")
    private String storageRepositoryFolder;
    
    private Logger logger = LoggerFactory.getLogger(RestFileStorageController.class);
    
    @GetMapping("/{fileNameWithExtension}")
    public ResponseEntity<String> getFile(@PathVariable String fileNameWithExtension) {
        logger.info("Storage folder configuration:" + storageRepositoryFolder);
        return new ReadFileServiceHandler().doAction(fileNameWithExtension);
    }

    @PutMapping("/{fileNameWithExtension}")
    public ResponseEntity<String> putFile(@PathVariable String fileNameWithExtension) {
        return ResponseEntity.ok("Updating file " + fileNameWithExtension);
    }
    
    @DeleteMapping("/{fileNameWithExtension}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileNameWithExtension) {
        return ResponseEntity.ok("Deleting file " + fileNameWithExtension);
    }
}