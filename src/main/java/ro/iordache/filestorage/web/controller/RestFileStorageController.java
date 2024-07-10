package ro.iordache.filestorage.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import ro.iordache.filestorage.repository.FileSystemStorageService;
import ro.iordache.filestorage.rest.FileAccessRequest;
import ro.iordache.filestorage.rest.ValidationHelper;
import ro.iordache.filestorage.rest.ValidationHelper.FileNameFormatException;
import ro.iordache.filestorage.rest.impl.DeleteFileServiceHandler;
import ro.iordache.filestorage.rest.impl.PutFileServiceHandler;
import ro.iordache.filestorage.rest.impl.ReadFileServiceHandler;

@RestController
@RequestMapping("/api/v1/files")
public class RestFileStorageController {
    
    public static final Pattern ALLOWED_FILENAME_FORMAT_PATTERN = Pattern.compile("[a-zA-Z0-9_\\-][a-zA-Z0-9_\\-\\.]{0,63}");
    
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
    public ResponseEntity getFile(@PathVariable String fileNameWithExtension, HttpServletRequest request) {
        try {
            FileAccessRequest fileAccessRequest = ValidationHelper.validateRequest(fileNameWithExtension, request);
            return readHandler.doAction(fileAccessRequest);
        } catch (FileNameFormatException fnfe) {
            return ResponseEntity.badRequest().body(fnfe.getMessage());
        } catch (Exception e) { 
            logger.error("GET File error!", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/{fileNameWithExtension}")
    public ResponseEntity<String> putFile(@PathVariable String fileNameWithExtension, HttpServletRequest request) {
        try {
            FileAccessRequest fileAccessRequest = ValidationHelper.validateRequest(fileNameWithExtension, request);
            return putHandler.doAction(fileAccessRequest);
        } catch (FileNameFormatException fnfe) {
            return ResponseEntity.badRequest().body(fnfe.getMessage());
        } catch (Exception e) {
            logger.error("PUT File error!", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{fileNameWithExtension}")
    public ResponseEntity deleteFile(@PathVariable String fileNameWithExtension, HttpServletRequest request) {
        try {
            FileAccessRequest fileAccessRequest = ValidationHelper.validateRequest(fileNameWithExtension, request);
            return deleteHandler.doAction(fileAccessRequest);
        } catch (FileNameFormatException fnfe) {
            return ResponseEntity.badRequest().body(fnfe.getMessage());
        } catch (Exception e) { 
            logger.error("DELETE File error!", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        
    }
    
    @GetMapping(path="/size", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> getStorageSize() {
        
        Map<String, Long> response = new HashMap<String, Long>();
        response.put("numFiles", storageService.getSize());
        
        return response;
    }
    
    @GetMapping(path="/enum/{globPattern}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getEnum(@PathVariable String globPattern, 
            @RequestParam(defaultValue = "0") long startIndex, @RequestParam(defaultValue="1000") long pageSize) {
        return storageService.enumerate(globPattern, startIndex, pageSize);
    }
}