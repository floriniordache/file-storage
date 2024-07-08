package ro.iordache.filestorage.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import ro.iordache.filestorage.repository.FileSystemStorageService;
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
    
    @Autowired
    private FileSystemStorageService storageService;
    
    @GetMapping("/{fileNameWithExtension}")
    public ResponseEntity getFile(@PathVariable String fileNameWithExtension) {
        return readHandler.doAction(fileNameWithExtension, null);
    }

    @GetMapping(path="/size", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> getStorageSize() {
        
        Map<String, Long> response = new HashMap<String, Long>();
        response.put("numFiles", storageService.getSize());
        
        return response;
    }
    
    @PutMapping("/{fileNameWithExtension}")
    public ResponseEntity<String> putFile(@PathVariable String fileNameWithExtension, HttpServletRequest request) {
        return putHandler.doAction(fileNameWithExtension, request);
    }
    
    @DeleteMapping("/{fileNameWithExtension}")
    public ResponseEntity deleteFile(@PathVariable String fileNameWithExtension) {
        return deleteHandler.doAction(fileNameWithExtension, null);
    }
}