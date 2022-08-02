package com.ssafy.daero.common.controller;

import com.ssafy.daero.common.service.ImageService;
import com.ssafy.daero.common.vo.ImageVo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/image")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> imagePost(@RequestParam MultipartFile file) {
        ImageVo imageVo = this.imageService.uploadFile(file);
        switch (imageVo.getResult()) {
            case SUCCESS:
                return new ResponseEntity<>(imageVo.getDownloadUrl(), HttpStatus.OK);
            case SERVER_ERROR:
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            case BAD_REQUEST:
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/download/{file_name}")
    public ResponseEntity<byte[]> imageGet(@PathVariable("file_name") String fileName) {
        ImageVo imageVo = this.imageService.downloadFile(fileName);
        HttpHeaders headers = new HttpHeaders();
        switch (imageVo.getResult()) {
            case SUCCESS:
                try {
                    headers.add("Content-Type", imageVo.getFile().getContentType());
                    return new ResponseEntity<>(imageVo.getFile().getBytes(), headers, HttpStatus.OK);
                } catch (IOException e) {
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            case FILE_NOT_FOUND:
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            case BAD_REQUEST:
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
