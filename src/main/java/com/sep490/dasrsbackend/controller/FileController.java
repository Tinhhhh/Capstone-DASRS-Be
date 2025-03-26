package com.sep490.dasrsbackend.controller;
import com.sep490.dasrsbackend.service.FirebaseStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FirebaseStorageService firebaseStorageService;

    public FileController(FirebaseStorageService firebaseStorageService) {
        this.firebaseStorageService = firebaseStorageService;
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        String url = firebaseStorageService.uploadFile(file, "avatars");
        return ResponseEntity.ok(url);
    }

    @PostMapping("/upload-map")
    public ResponseEntity<String> uploadMap(@RequestParam("file") MultipartFile file) throws IOException {
        String url = firebaseStorageService.uploadFile(file, "maps");
        return ResponseEntity.ok(url);
    }

    @PostMapping("/upload-video")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {
        String url = firebaseStorageService.uploadFile(file, "videos");
        return ResponseEntity.ok(url);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("fileName") String fileName) throws IOException {
        byte[] file = firebaseStorageService.downloadFile(fileName);
        return ResponseEntity.ok(file);
    }
}

