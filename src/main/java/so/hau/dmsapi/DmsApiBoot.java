package so.hau.dmsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DmsApiBoot {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(DmsApiBoot.class);
        app.run(args);
    }
}
