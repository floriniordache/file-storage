package ro.iordache.filestorage.rest.impl;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ro.iordache.filestorage.repository.FileSystemStorageService;
import ro.iordache.filestorage.rest.EnumOperationResult;
import ro.iordache.filestorage.rest.EnumServiceHandler;
import ro.iordache.filestorage.rest.RESTApiResult;
import ro.iordache.filestorage.rest.SizeOperationResult;

@Service
public class EnumServiceHandlerImpl implements EnumServiceHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(EnumServiceHandlerImpl.class);

    @Autowired
    private FileSystemStorageService storageService;

    public SizeOperationResult getRepositorySize() {
        return SizeOperationResult.build(RESTApiResult.OK, storageService.getSize());
    }


    public EnumOperationResult enumerate(Pattern regexPattern, long startIndex, long pageSize) {
        List<String> matches = storageService.enumerate(regexPattern, startIndex, pageSize);
        
        EnumOperationResult enumResult = EnumOperationResult.build(RESTApiResult.OK, startIndex, matches);
        
        return enumResult;
    }

}
