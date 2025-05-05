package com.sep490.dasrsbackend.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

//    @Value("${firebase.credentials.path}")
//    private String credentialsPath;
//
//    @Value("${firebase.storage.bucket}")
//    private String storageBucket;
//
//    @PostConstruct
//    public void initialize() throws IOException {
//        FileInputStream serviceAccount = new FileInputStream(credentialsPath.replace("classpath:", "src/main/resources/"));
//
//        FirebaseOptions options = FirebaseOptions.builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .setStorageBucket(storageBucket)
//                .build();
//
//        if (FirebaseApp.getApps().isEmpty()) {
//            FirebaseApp.initializeApp(options);
//        }
//    }
}

