package ro.iordache.filestorage.web.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/files")
public class RestFileStorageController {
    
    @GetMapping("/{fileNameWithExtension}")
    public ResponseEntity<String> getFile(@PathVariable String fileNameWithExtension) {
        return ResponseEntity.ok("Requesting file " + fileNameWithExtension);
    }

    @PutMapping("/{fileNameWithExtension}")
    public ResponseEntity<String> putFile(@PathVariable String fileNameWithExtension) {
        return ResponseEntity.ok("Updating file " + fileNameWithExtension);
    }
    
    @DeleteMapping("/{fileNameWithExtension}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileNameWithExtension) {
        return ResponseEntity.ok("Deleting file " + fileNameWithExtension);
    }
}