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
import ro.iordache.filestorage.repository.util.FileSystemStorageHelperImpl;
import ro.iordache.filestorage.rest.FileAccessOperation;
import ro.iordache.filestorage.rest.FileAccessRequest;
import ro.iordache.filestorage.rest.FileAccessResult;

/**
 * Tests for the {@link ReadFileServiceHandler}
 */
@RunWith(SpringRunner.class)
public class ReadFileServiceHandlerTest {

    @MockBean
    private FileSystemStorageService storageService;
    
    @SpyBean
    private FileSystemStorageHelperImpl fileHelper;
    
    @SpyBean
    private ReadFileServiceHandler readHandler;
    
    @Test
    public void testGetOperationType() {
        Assert.assertEquals("Invalid operation type for Read handler", FileAccessOperation.READ, readHandler.getOperationType());
    }
    
    @Test
    public void testHandlerResultNotFound() {
        FileAccessRequest accessRequest = Mockito.mock(FileAccessRequest.class);
        FileAccessResult result = readHandler.doAction(accessRequest);
        
        Assert.assertEquals("Get result should be not found for missing file!",FileAccessResult.NOT_FOUND , result.getType());
    }
    
    @Test
    public void testHandlerResultOk() {
        try {
            String mockFileName = "mockfile.txt";
            FileAccessRequest accessRequest = Mockito.mock(FileAccessRequest.class);
            InputStream mockFileInputStream = Mockito.mock(InputStream.class);
            
            Mockito.when(accessRequest.getFileName()).thenReturn(mockFileName);
            Mockito.when(storageService.getFileContent(mockFileName)).thenReturn(mockFileInputStream);
            
            FileAccessResult result = readHandler.doAction(accessRequest);
            
            Assert.assertEquals("Result should be success",FileAccessResult.OK, result.getType());
            
            Assert.assertEquals("Action result should contain the file's input stream", mockFileInputStream, result.getInputStream().getInputStream());
        } catch (Exception e) {
            Assert.fail("Exception thrown, test failed!");
        }
    }
    
    @Test
    public void testHandlerResultCached() {
        try {
            String mockFileName = "mockfile.txt";
            FileAccessRequest accessRequest = Mockito.mock(FileAccessRequest.class);
            InputStream mockFileInputStream = Mockito.mock(InputStream.class);
            
            Mockito.when(accessRequest.getFileName()).thenReturn(mockFileName);
            Mockito.when(accessRequest.checkNotModified(Mockito.anyLong())).thenReturn(true);
            Mockito.when(storageService.getFileContent(mockFileName)).thenReturn(mockFileInputStream);
            Mockito.when(storageService.getFileLastModified(mockFileName)).thenReturn(System.currentTimeMillis());
            
            FileAccessResult result = readHandler.doAction(accessRequest);
            
            Assert.assertEquals("Result should be success",FileAccessResult.NOT_MODIFIED, result.getType());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception thrown, test failed!");
        }
    }
    
    @Test
    public void testHandlerResultException() {
        try {
            String mockFileName = "mockfile.txt";
            FileAccessRequest accessRequest = Mockito.mock(FileAccessRequest.class);
            
            Mockito.when(accessRequest.getFileName()).thenReturn(mockFileName);
            Mockito.when(storageService.getFileContent(mockFileName)).thenThrow(new IOException("mock exception"));
            
            FileAccessResult result = readHandler.doAction(accessRequest);
            
            Assert.assertEquals("Internal Server error is expected",FileAccessResult.INTERNAL_ERROR , result.getType());
        } catch (Exception e) {
            Assert.fail("Exception thrown, test failed!");
        }
    }
}
