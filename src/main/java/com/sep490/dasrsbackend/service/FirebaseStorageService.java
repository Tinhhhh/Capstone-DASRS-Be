package com.sep490.dasrsbackend.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FirebaseStorageService {

    /**
     * Uploads a file (image or video) to Firebase Storage under the specified directory.
     *
     * @param file      The file to upload.
     * @param directory The directory to store the file in (e.g., avatars, maps, videos).
     * @return The public URL of the uploaded file.
     * @throws IOException If an I/O error occurs during upload.
     */
    public String uploadFile(MultipartFile file, String directory) throws IOException {
        String fileName = directory + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        Bucket bucket = StorageClient.getInstance().bucket();
        bucket.create(fileName, file.getInputStream(), file.getContentType());
        return String.format("https://storage.googleapis.com/%s/%s", bucket.getName(), fileName);
    }

    /**
     * Downloads a file from Firebase Storage.
     *
     * @param fileName The name of the file to download.
     * @return The byte array of the file content.
     * @throws IOException If an I/O error occurs during download.
     */
    public byte[] downloadFile(String fileName) throws IOException {
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get(fileName);
        if (blob == null) {
            throw new IOException("File not found: " + fileName);
        }
        return blob.getContent();
    }
}

