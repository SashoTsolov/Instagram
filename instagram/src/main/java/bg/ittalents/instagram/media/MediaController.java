package bg.ittalents.instagram.media;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;

@RestController
public class MediaController {

    @Autowired
    private MediaService mediaService;

//    @PostMapping("/media")
//    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file, HttpSession s){
//        return mediaService.upload(file, getLoggedId(s));
//    }
//
//    @SneakyThrows
//    @GetMapping("/media/{fileName}")
//    public void download(@PathVariable("fileName") String fileName, HttpServletResponse resp){
//        File f = mediaService.download(fileName);
//        Files.copy(f.toPath(), resp.getOutputStream());
//    }
}
