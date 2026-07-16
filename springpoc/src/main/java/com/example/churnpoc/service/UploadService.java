package com.example.churnpoc.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.example.churnpoc.dto.UploadReceipt;

public interface UploadService {

    UploadReceipt load(MultipartFile theFile) throws IOException;
}
