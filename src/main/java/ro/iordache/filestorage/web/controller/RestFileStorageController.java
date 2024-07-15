package ro.iordache.filestorage.web.controller;

import java.net.URI;
import java.text.SimpleDateFormat;
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
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import ro.iordache.filestorage.rest.EnumOperationResult;
import ro.iordache.filestorage.rest.EnumServiceHandler;
import ro.iordache.filestorage.rest.FileAccessOperation;
import ro.iordache.filestorage.rest.FileAccessRequest;
import ro.iordache.filestorage.rest.FileAccessResult;
import ro.iordache.filestorage.rest.FileAccessServiceHandler;
import ro.iordache.filestorage.rest.SizeOperationResult;
import ro.iordache.filestorage.rest.ValidationHelper;
import ro.iordache.filestorage.rest.ValidationHelper.FileNameFormatException;

/**
 * REST API controller for the file storage server operations
 */
@RestController
@RequestMapping("/api/v1/files")
public class RestFileStorageController {
    
    private static final SimpleDateFormat MODIFIED_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy KK:mm:ss a Z");
    
    private static final Logger logger = LoggerFactory.getLogger(RestFileStorageController.class);
    
    private static final int MAX_PAGE_SIZE = 1000;
    
    @Autowired
    private EnumServiceHandler enumService;
    
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
    private ResponseEntity handleFileOperation(FileAccessOperation operation, String fileName, HttpServletRequest request) {
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
                    
                    BodyBuilder responseBuilder = ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM);
                    if (fileAccessResult.getLastModified() > 0) {
                        responseBuilder.lastModified(fileAccessResult.getLastModified());
                    }
                    restResponse = responseBuilder.body(fileAccessResult.getInputStream());

                } else {
                    restResponse = ResponseEntity.ok().build();
                }
                break;
            case FileAccessResult.NOT_FOUND:
                restResponse = ResponseEntity.notFound().build();
                break;
            case FileAccessResult.NOT_MODIFIED:
                restResponse = ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
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
        return handleFileOperation(FileAccessOperation.READ, fileNameWithExtension, request);
    }

    @PutMapping("/{fileNameWithExtension}")
    public ResponseEntity putFile(@PathVariable String fileNameWithExtension, HttpServletRequest request) {
        return handleFileOperation(FileAccessOperation.CREATE_UPDATE, fileNameWithExtension, request);
    }
    
    @DeleteMapping("/{fileNameWithExtension}")
    public ResponseEntity deleteFile(@PathVariable String fileNameWithExtension, HttpServletRequest request) {
        return handleFileOperation(FileAccessOperation.DELETE, fileNameWithExtension, request);
    }
    
    @GetMapping(path="/size", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getStorageSize() {
        SizeOperationResult sizeOpResult = enumService.getRepositorySize();
        
        return buildJSONResponse(sizeOpResult);
    }
    
    @GetMapping(path="/enum/{regex}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getEnum(@PathVariable String regex, 
            @RequestParam(defaultValue = "0") long startIndex, @RequestParam(defaultValue="1000") int pageSize) {
        try {
            Pattern regexPattern = Pattern.compile(regex);
            
            if (startIndex < 0) {
                startIndex = 0;
            }
            
            if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
                pageSize = MAX_PAGE_SIZE;
            }
            
            EnumOperationResult enumOpResult = enumService.enumerate(regexPattern, startIndex, pageSize);
            
            return buildJSONResponse(enumOpResult);
        } catch (PatternSyntaxException pse) {
            logger.debug("Invalid regular expression pattern {}", regex, pse);
            return ResponseEntity.badRequest().body("Invalid regular expression pattern!");
        }

    }
    
    private ResponseEntity buildJSONResponse(Object result) {
        try {
            ObjectMapper objMapper = new ObjectMapper();
            return ResponseEntity.ok(objMapper.writeValueAsString(result));
        } catch (JsonProcessingException jspe) {
            return ResponseEntity.internalServerError().body("Error serializing to JSON!");
        }
    }
}