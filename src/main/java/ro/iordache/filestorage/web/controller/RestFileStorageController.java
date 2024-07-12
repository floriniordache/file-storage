package ro.iordache.filestorage.web.controller;

import java.net.URI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import ro.iordache.filestorage.rest.FileAccessOperation;
import ro.iordache.filestorage.rest.FileAccessRequest;
import ro.iordache.filestorage.rest.FileAccessResult;
import ro.iordache.filestorage.rest.FileAccessServiceHandler;
import ro.iordache.filestorage.rest.ValidationHelper;
import ro.iordache.filestorage.rest.ValidationHelper.FileNameFormatException;

/**
 * REST API controller for the file storage server operations
 */
@RestController
@RequestMapping("/api/v1/files")
public class RestFileStorageController {
    
    private static final Logger logger = LoggerFactory.getLogger(RestFileStorageController.class);
    
    @Autowired
    private FileSystemStorageService storageService;
    
    private Map<FileAccessOperation, FileAccessServiceHandler> fileAccessOpsHandlers;
    
    @Autowired
    public RestFileStorageController(List<FileAccessServiceHandler> handlerList) {
        fileAccessOpsHandlers = new HashMap<FileAccessOperation, FileAccessServiceHandler>();
        
        for (FileAccessServiceHandler fileServiceHandler : handlerList) {
            fileAccessOpsHandlers.put(fileServiceHandler.getOperationType(), fileServiceHandler);
        }
    }
    
    /**
     * Handle a file access operation type request
     * 
     * @param operation - the {@link FileAccessOperation} describing the file operation type
     * @param fileName - the file name to execute the operation on
     * @param request - the current {@link HttpServletRequest}
     * 
     * @return a {@link ResponseEntity} result
     */
    private ResponseEntity handle(FileAccessOperation operation, String fileName, HttpServletRequest request) {
        try {
            
            // Validate the filename against allowed formats
            FileAccessRequest fileAccessRequest = ValidationHelper.validateRequest(fileName, request);
            
            // lookup a handler that can perform the desired operation on the file
            FileAccessServiceHandler fileServiceHandler = fileAccessOpsHandlers.get(operation);
            if (fileServiceHandler == null) {
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
            }
            
            // perform the file-related operation
            FileAccessResult fileAccessResult = fileServiceHandler.doAction(fileAccessRequest);
            
            // build the appropriate response entity based on the result of the handler operation
            ResponseEntity restResponse;
            switch(fileAccessResult.getType()) {
            case FileAccessResult.CREATED:
                restResponse = ResponseEntity.created(URI.create(request.getRequestURI())).build();
                break;
            case FileAccessResult.OK:
                if (fileAccessResult.getInputStream() != null) {
                    restResponse = ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .body(fileAccessResult.getInputStream());
                } else {
                    restResponse = ResponseEntity.ok().build();
                }
                break;
            case FileAccessResult.NOT_FOUND:
                restResponse = ResponseEntity.notFound().build();
                break;
            default:
                restResponse = ResponseEntity.internalServerError().build();
                break;
            }
            
            return restResponse;
        } catch (FileNameFormatException fnfe) {
            return ResponseEntity.badRequest().body(fnfe.getMessage());
        } catch (Exception e) { 
            logger.error("{} REST call error!", operation.toString(), e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    
    @GetMapping("/{fileNameWithExtension}")
    public ResponseEntity getFile(@PathVariable String fileNameWithExtension, HttpServletRequest request) {
        return handle(FileAccessOperation.READ, fileNameWithExtension, request);
    }

    @PutMapping("/{fileNameWithExtension}")
    public ResponseEntity<String> putFile(@PathVariable String fileNameWithExtension, HttpServletRequest request) {
        return handle(FileAccessOperation.CREATE_UPDATE, fileNameWithExtension, request);
    }
    
    @DeleteMapping("/{fileNameWithExtension}")
    public ResponseEntity deleteFile(@PathVariable String fileNameWithExtension, HttpServletRequest request) {
        return handle(FileAccessOperation.DELETE, fileNameWithExtension, request);
    }
    
    @GetMapping(path="/size", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> getStorageSize() {
        
        Map<String, Long> response = new HashMap<String, Long>();
        response.put("numFiles", storageService.getSize());
        
        return response;
    }
    
    @GetMapping(path="/enum/{regex}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getEnum(@PathVariable String regex, 
            @RequestParam(defaultValue = "0") long startIndex, @RequestParam(defaultValue="1000") long pageSize) {
        try {
            Pattern regexPattern = Pattern.compile(regex);
            
            return ResponseEntity.ok(storageService.enumerate(regexPattern, startIndex, pageSize));
        } catch (PatternSyntaxException pse) {
            return ResponseEntity.badRequest().body("Invalid regular expression pattern!");
        }

    }
}