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
 * Tests for the {@link DeleteFileServiceHandler}
 */
@RunWith(SpringRunner.class)
public class DeleteFileServiceHandlerTest {

    @MockBean
    private FileSystemStorageService storageService;
    
    @SpyBean
    private DeleteFileServiceHandler deleteHandler;
    
    @Test
    public void testDeleteOperationType() {
        Assert.assertEquals("Invalid operation type for Delete handler", FileAccessOperation.DELETE, deleteHandler.getOperationType());
    }
    
    @Test
    public void testHandlerResultNotFoundOk() {
        try {
            String mockFileName = "mockfile.txt";
            FileAccessRequest accessRequest = Mockito.mock(FileAccessRequest.class);
            
            Mockito.when(accessRequest.getFileName()).thenReturn(mockFileName);
            Mockito.when(storageService.deleteFile(mockFileName)).thenReturn(false).thenReturn(true);
            
            // first test the not found case
            FileAccessResult result = deleteHandler.doAction(accessRequest);
            Assert.assertEquals("Result should be success",FileAccessResult.NOT_FOUND, result.getType());
            
            // second time should return success
            result = deleteHandler.doAction(accessRequest);
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
            
            Mockito.when(accessRequest.getFileName()).thenReturn(mockFileName);
            Mockito.when(storageService.deleteFile(mockFileName)).thenThrow(new IOException("mock exception"));
            
            FileAccessResult result = deleteHandler.doAction(accessRequest);
            
            Assert.assertEquals("Internal Server error is expected",FileAccessResult.INTERNAL_ERROR , result.getType());
        } catch (Exception e) {
            Assert.fail("Exception thrown, test failed!");
        }
    }
}
