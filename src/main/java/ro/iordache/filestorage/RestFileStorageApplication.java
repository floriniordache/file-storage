package ro.iordache.filestorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RestFileStorageApplication {

  public static void main(String[] args) {
    SpringApplication.run(RestFileStorageApplication.class, args);
  }

}
