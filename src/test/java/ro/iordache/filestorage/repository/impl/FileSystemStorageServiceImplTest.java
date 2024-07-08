package ro.iordache.filestorage.repository.impl;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import ro.iordache.filestorage.repository.util.FileSystemStorageHelperImpl;

@RunWith(SpringRunner.class)
public class FileSystemStorageServiceImplTest {

    @SpyBean
    private FileSystemStorageHelperImpl storageHelper;
    
    @SpyBean
    private FileSystemStorageServiceImpl fileStorageService;
    
    @Before
    public void setUp() throws IOException {
        
        File f1 = storageHelper.getStorageFile("f1.txt");
        f1.createNewFile();
        f1.deleteOnExit();
        
        File f2 = storageHelper.getStorageFile("f2.txt");
        f2.createNewFile();
        f2.deleteOnExit();
        
        fileStorageService.init();
    }

    @Test
    public void testScanRepo() {
        long indexedSize = fileStorageService.getSize();
        
        Assert.assertEquals("Index should contain two entries!", 2, indexedSize);
    }
}
