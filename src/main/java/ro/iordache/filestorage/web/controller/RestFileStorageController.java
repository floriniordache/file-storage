package ro.iordache.filestorage.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import ro.iordache.filestorage.rest.impl.DeleteFileServiceHandler;
import ro.iordache.filestorage.rest.impl.PutFileServiceHandler;
import ro.iordache.filestorage.rest.impl.ReadFileServiceHandler;

@RestController
@RequestMapping("/api/v1/files")
public class RestFileStorageController {
    
    private static final Logger logger = LoggerFactory.getLogger(RestFileStorageController.class);
    
    @Autowired
    private ReadFileServiceHandler readHandler;
    
    @Autowired
    private PutFileServiceHandler putHandler;
    
    @Autowired
    private DeleteFileServiceHandler deleteHandler;
    
    /*@PostConstruct
    public void init() {
        logger.info("Controller initializing...");
        File storageRoot = new File("./" + storageRepositoryFolder);
        if (!storageRoot.exists()) {
            storageRoot.mkdirs();
        }
        logger.info("Storage folder configuration:" + storageRepositoryFolder);
    }*/
    
    @GetMapping("/{fileNameWithExtension}")
    public ResponseEntity getFile(@PathVariable String fileNameWithExtension) {
        return readHandler.doAction(fileNameWithExtension, null);
    }

    @PutMapping("/{fileNameWithExtension}")
    public ResponseEntity<String> putFile(@PathVariable String fileNameWithExtension, HttpServletRequest request) {
        return putHandler.doAction(fileNameWithExtension, request);
    }
    
    @DeleteMapping("/{fileNameWithExtension}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileNameWithExtension) {
        return deleteHandler.doAction(fileNameWithExtension, null);
    }
}