package ro.iordache.filestorage.repository.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import ro.iordache.filestorage.repository.impl.index.StorageIndex;
import ro.iordache.filestorage.repository.util.FileSystemStorageHelperImpl;

@RunWith(SpringRunner.class)
public class FileSystemStorageServiceImplTest {

    @SpyBean
    private FileSystemStorageHelperImpl storageHelper;
    
    @SpyBean
    private FileSystemStorageServiceImpl fileStorageService;
    
    @SpyBean
    private StorageIndex storageIndex;
    
    @Before
    public void setUp() throws IOException {
        
        Path f1 = storageHelper.getStorageFile("f1.txt");
        Files.createFile(f1);
        
        Path f2 = storageHelper.getStorageFile("f2.txt");
        Files.createFile(f2);
        
        fileStorageService.init();
    }
    
    @After
    public void cleanUp() throws IOException {
        Path f1 = storageHelper.getStorageFile("f1.txt");
        Files.deleteIfExists(f1);
        
        Path f2 = storageHelper.getStorageFile("f2.txt");
        Files.deleteIfExists(f2);
    }

    @Test
    public void testScanRepo() {
        long indexedSize = fileStorageService.getSize();
        
        Assert.assertEquals("Index should contain two entries!", 2, indexedSize);
    }
}
