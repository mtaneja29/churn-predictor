package com.example.churnpoc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.churnpoc.service.UploadService;

@Controller
@RequestMapping("/upload")
public class UploadController {

    private UploadService uploadService;

    @Autowired
    public UploadController(UploadService theUploadService) {
        uploadService = theUploadService;
    }

    @GetMapping
    public String showUploadPage() {
        return "upload";
    }

    @PostMapping
    public String handleUpload(@RequestParam("file") MultipartFile theFile, Model theModel) {
        if (theFile.isEmpty()) {
            theModel.addAttribute("errorMessage", "Please choose a CSV file first.");
            return "upload";
        }
        try {
            theModel.addAttribute("receipt", uploadService.load(theFile));
        }
        catch (Exception exc) {
            theModel.addAttribute("errorMessage", "Upload failed: " + exc.getMessage());
        }
        return "upload";
    }
}
