package ro.iordache.filestorage.rest;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpHeaders;

import jakarta.servlet.http.HttpServletRequest;

/**
 * File operation RST API request validation helper
 */
public class FileRequestValidationHelper {
    public static final class FileNameFormatException extends RuntimeException {
        public FileNameFormatException() {
            super("File Name format is invalid!");
        }
    }
    
    public static final Pattern ALLOWED_FILENAME_FORMAT_PATTERN = Pattern.compile("[a-zA-Z0-9_\\-][a-zA-Z0-9_\\-\\.]{0,63}");
    
    /**
     * Validates a file operation type request by checking for allowed filename pattern
     * 
     * 
     * @param fileName - the file name
     * @param httpRequest - the current http request
     * @return a {@link FileAccessRequest} object
     * @throws FileNameFormatException if filename does not match supported format 
     * @throws IOException on IO errors
     */
    public static FileAccessRequest validateRequest(String fileName, HttpServletRequest httpRequest) 
            throws FileNameFormatException, IOException{
        Matcher filenameMatcher = ALLOWED_FILENAME_FORMAT_PATTERN.matcher(fileName);
        if (!filenameMatcher.matches()) {
            throw new FileNameFormatException();
        }

        long lastModifiedFromRequest = httpRequest.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
        
        return new FileAccessRequest(fileName, httpRequest.getInputStream(), lastModifiedFromRequest);
    }
}
