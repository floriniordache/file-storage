package ro.iordache.filestorage.rest.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import ro.iordache.filestorage.repository.FileSystemStorageService;
import ro.iordache.filestorage.rest.EnumOperationResult;
import ro.iordache.filestorage.rest.SizeOperationResult;

/**
 * Tests for the {@link EnumServiceHandlerImpl} hander
 */
@RunWith(SpringRunner.class)
public class EnumServiceHandlerImplTest {

    @MockBean
    private FileSystemStorageService storageService;
    
    @SpyBean
    private EnumServiceHandlerImpl enumService;
    
    @Test
    public void testGetRepositorySize() {
        long mockSize = System.currentTimeMillis();
        
        Mockito.when(storageService.getSize()).thenReturn(mockSize);
        
        SizeOperationResult result = enumService.getRepositorySize();
        
        Assert.assertEquals("Invalid repository size returned!", mockSize, result.getFilesCount());
    }
    
    @Test
    public void testEnumeration() {
        List<String> mockResults = new ArrayList<String>();
        mockResults.add("file1.txt");
        mockResults.add("file2.txt");
        
        Pattern mockPattern = Mockito.mock(Pattern.class);
        
        Mockito.when(storageService.enumerate(mockPattern, 0, 100)).thenReturn(mockResults);
        
        EnumOperationResult result = enumService.enumerate(mockPattern, 0, 100);
        
        Assert.assertEquals("Invalid result size returned!", 2, result.getItemCount());
        
        Assert.assertTrue("Invalid result contents!", mockResults.containsAll(result.getMatchingItems())
                && result.getMatchingItems().containsAll(mockResults));
    }
}
