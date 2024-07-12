package ro.iordache.filestorage.rest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.servlet.http.HttpServletRequest;
import ro.iordache.filestorage.rest.ValidationHelper.FileNameFormatException;

/**
 * Tests the {@link ValidationHelper} class
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationHelperTest {
    
    @Mock
    private HttpServletRequest mockRequest;
    
    @Test
    public void testValidFormats() {
        testValidFilenameFormat("aAzZ09-_.txt");
        testValidFilenameFormat("012345678901234567890123456789012345678901234567890123456789.123");
    }
    
    @Test
    public void testInvalidFormats() {
        testInvalidFilenameFormat("$#.txt");
        testInvalidFilenameFormat("0123456789012345678901234567890123456789012345678901234567890.123");
    }
    
    private void testValidFilenameFormat(String fileName) {
        try {
            FileAccessRequest result = ValidationHelper.validateRequest(fileName, mockRequest);
            Assert.assertEquals("Filename should be allowed!", result.getFileName(), fileName);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception thrown, test failed");
        }
    }
    
    private void testInvalidFilenameFormat(String fileName) {
        boolean exceptionThrown = false;
        try {
            ValidationHelper.validateRequest(fileName, mockRequest);
            Assert.fail("Filename should not be allowed!");
            
        } catch (FileNameFormatException fnfe) {
            exceptionThrown = true;
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception thrown, test failed");
        }
        
        Assert.assertTrue("Validation exception should be thrown", exceptionThrown);
    }
}
