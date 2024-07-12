package ro.iordache.filestorage.rest.impl;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import ro.iordache.filestorage.repository.FileSystemStorageService;
import ro.iordache.filestorage.rest.FileAccessOperation;
import ro.iordache.filestorage.rest.FileAccessRequest;
import ro.iordache.filestorage.rest.FileAccessResult;

/**
 * Tests for the {@link PutFileServiceHandler}
 */
@RunWith(SpringRunner.class)
public class PutFileServiceHandlerTest {

    @MockBean
    private FileSystemStorageService storageService;
    
    @SpyBean
    private PutFileServiceHandler putHandler;
    
    @Test
    public void testPutOperationType() {
        Assert.assertEquals("Invalid operation type for Put handler", FileAccessOperation.CREATE_UPDATE, putHandler.getOperationType());
    }
    
    @Test
    public void testHandlerResultCreatedUpdated() {
        try {
            String mockFileName = "mockfile.txt";
            FileAccessRequest accessRequest = Mockito.mock(FileAccessRequest.class);
            InputStream mockFileInputStream = Mockito.mock(InputStream.class);
            
            Mockito.when(accessRequest.getFileName()).thenReturn(mockFileName);
            Mockito.when(accessRequest.getInputStream()).thenReturn(mockFileInputStream);
            
            Mockito.when(storageService.storeFile(mockFileName, mockFileInputStream)).thenReturn(true).thenReturn(false);
            
            // first test the newly created case
            FileAccessResult result = putHandler.doAction(accessRequest);
            Assert.assertEquals("Result should be new created file",FileAccessResult.CREATED, result.getType());
            
            // second time should return file updated success
            result = putHandler.doAction(accessRequest);
            Assert.assertEquals("Result should be success",FileAccessResult.OK, result.getType());
        } catch (Exception e) {
            Assert.fail("Exception thrown, test failed!");
        }
    }
    
    @Test
    public void testHandlerResultException() {
        try {
            String mockFileName = "mockfile.txt";
            FileAccessRequest accessRequest = Mockito.mock(FileAccessRequest.class);
            InputStream mockFileInputStream = Mockito.mock(InputStream.class);
            
            Mockito.when(accessRequest.getFileName()).thenReturn(mockFileName);
            Mockito.when(accessRequest.getInputStream()).thenReturn(mockFileInputStream);
            
            Mockito.when(storageService.storeFile(mockFileName, mockFileInputStream)).thenThrow(RuntimeException.class);
            
            FileAccessResult result = putHandler.doAction(accessRequest);
            
            Assert.assertEquals("Internal Server error is expected",FileAccessResult.INTERNAL_ERROR , result.getType());
        } catch (Exception e) {
            Assert.fail("Exception thrown, test failed!");
        }
    }
}
