package com.example.backend.manager;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FileStorageResult {
    private String dir;
    private String fileName;
    // String imageUrl = pic.getDir() + pic.getFileName();
}