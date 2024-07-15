package ro.iordache.filestorage.repository.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;

import ro.iordache.filestorage.repository.impl.index.StorageIndex;
import ro.iordache.filestorage.repository.util.FileSystemStorageHelperImpl;

@RunWith(SpringRunner.class)
public class FileSystemStorageServiceImplTest {
    
    private static final Logger logger = LoggerFactory.getLogger(FileSystemStorageServiceImplTest.class);

    private final int FILES_COUNT = 10;
    
    @SpyBean
    private FileSystemStorageHelperImpl storageHelper;
    
    @SpyBean
    private FileSystemStorageServiceImpl fileStorageService;
    
    @SpyBean
    private StorageIndex storageIndex;
    
    private List<String> createdFileList;

    @Before
    public void setUp() throws IOException {
        createdFileList = new ArrayList<String>();
    }
    
    @After
    public void cleanUp() throws IOException {
        if (createdFileList != null) {
            for (String filename : createdFileList) {
                Path file = storageHelper.getStorageFile(filename);
                Files.deleteIfExists(file);
            }
        }
    }

    @Test
    public void testScanRepo() throws IOException {
        fileStorageService.init();
        long currentSize = fileStorageService.getSize();
        
        // create some new files
        List<String> newFiles = createEmptyFiles(FILES_COUNT);
        createdFileList.addAll(newFiles);
        
        // call init again to trigger a reindexing repository
        fileStorageService.init();
        long newIndexedSize = fileStorageService.getSize();
        
        Assert.assertEquals("Index should contain two new entries!", currentSize + FILES_COUNT, newIndexedSize);
    }

    @Test
    public void testStoreFileError() throws Exception {
        String fileName = "testError.file";
        boolean exceptionThrown = false;
        try {
            fileStorageService.storeFile("testError.file", null);
        } catch (Exception e) {
            exceptionThrown = true;
        }

        Assert.assertTrue("No exception thrown on store file error!", exceptionThrown);
        
        // check null response from getting file content
        InputStream fileIS = fileStorageService.getFileContent(fileName);
        
        Assert.assertNull("InputStream for non-existing file should be null!", fileIS);
    }
    
    @Test
    public void testStoreRetrieveDelete() throws Exception {
        long initialSize = fileStorageService.getSize();
        // create a bunch of new files
        Map<String, String> addedFiles = new HashMap<String, String>();
        for (int i = 0 ; i < FILES_COUNT ; i++) {
            String fileName = String.valueOf(System.nanoTime() + ".file");
            String fileContents = RandomStringUtils.randomAlphabetic(1024, 2048);
            
            addedFiles.put(fileName, fileContents);
            
            InputStream contentsStream = new ByteArrayInputStream(fileContents.getBytes(StandardCharsets.UTF_8));
            
            // store the files
            fileStorageService.storeFile(fileName, contentsStream);
            
            createdFileList.add(fileName);
        }
        
        // check the new size
        Assert.assertEquals("Invalid storage size count!", initialSize + FILES_COUNT, fileStorageService.getSize());
        
        // retrieve the files and check their contents
        for (String newFileName : addedFiles.keySet()) {
            InputStream fileContents = fileStorageService.getFileContent(newFileName);
            
            StringBuilder build = new StringBuilder();
            byte[] buf = new byte[1024];
            int length;
            try {
                while ((length = fileContents.read(buf)) != -1) {
                    build.append(new String(buf, 0, length, StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                logger.error("Error reading file contents!", e);
            }
            
            Assert.assertEquals("Invalid stored file contents!", addedFiles.get(newFileName), build.toString());
        }
        
        // try out the enumerate functionality and check that all files are returned
        List<String> allEnumFiles = fileStorageService.enumerate(Pattern.compile(".*"), 0, 1000);
        Assert.assertTrue("Should contain all created files!", allEnumFiles.containsAll(createdFileList));
        
        // delete the newly added files
        int deleted = 0;
        List<String> removedFiles = new ArrayList<String>();
        for (String fileNameToDelete : addedFiles.keySet()) {
            boolean deleteSuccess = false;
            try {
                deleteSuccess = fileStorageService.deleteFile(fileNameToDelete);
            } catch (Exception e) {}
            
            if (deleteSuccess) {
                deleted++;
                removedFiles.add(fileNameToDelete);
                
                if (deleted >= addedFiles.size()) {
                    break;
                }
            }
        }
        
        // enumerate again
        allEnumFiles = fileStorageService.enumerate(Pattern.compile(".*"), 0, 1000);
        Assert.assertTrue("Enumeration still contains removed files after 5 invocations!", 
                verifyEnumDisjointedWithDelay(5, 100, removedFiles));
    }
    
    @Test
    public void testStoreDeleteStore() throws Exception {
        // create some files
        List<String> originalEmptyFiles = storeCreateEmptyFiles(FILES_COUNT);
        
        // delete them
        for (String fileName : originalEmptyFiles) {
            try {
                fileStorageService.deleteFile(fileName);
            } catch (Exception e) {}
        }
        
        // create some more
        List<String> otherEmptyFiles = storeCreateEmptyFiles(FILES_COUNT);
        
        // enumerate, should only find the last created ones
        List<String> allEnumFiles = fileStorageService.enumerate(Pattern.compile(".*"), 0, 1000);
        Assert.assertTrue("Enumeration still contains removed files after 5 invocations!", 
                verifyEnumDisjointedWithDelay(5, 100, originalEmptyFiles));
    }
    
    private boolean verifyEnumDisjointedWithDelay(int maxInvocations, long delayBetweenCalls, List<String> listFiles) {
        List<String> enumResult;
        int invocationCount = 0;
        
        while (invocationCount < maxInvocations) {
            enumResult = fileStorageService.enumerate(Pattern.compile(".*"), 0, 1000);
            
            if (Collections.disjoint(enumResult, listFiles)) {
                return true;
            }
            
            try {
                Thread.sleep(delayBetweenCalls);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            invocationCount++;
        }
        
        return false;
    }
    
    /**
     * Creates some empty files via the file storage service.storeFile call
     * @param count
     * @return a {@link List} of newly created files
     * @throws Exception
     */
    private List<String> storeCreateEmptyFiles(int count) throws Exception {
        // create a bunch of new files
        List<String> addedFiles = new ArrayList<String>();
        for (int i = 0 ; i < count ; i++) {
            String fileName = String.valueOf(System.nanoTime() + ".file");
            
            InputStream contentsStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            
            // store the files
            fileStorageService.storeFile(fileName, contentsStream);
            
            createdFileList.add(fileName);
        }
        
        return addedFiles;
    }
    
    private List<String> createEmptyFiles(int count) throws IOException {
        List<String> fileList = new ArrayList<String>();
        
        for (int i = 0 ; i < count ; i++) {
            String fileName = String.valueOf(System.nanoTime() + ".file");
            Path filePath = storageHelper.getStorageFile(fileName);
            Files.createFile(filePath);

            fileList.add(fileName);
        }
        
        return fileList;
    }
}
