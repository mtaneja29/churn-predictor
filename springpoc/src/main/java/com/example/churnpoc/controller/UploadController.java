package com.example.churnpoc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.client.RestClientException;

import com.example.churnpoc.service.ScanService;
import com.example.churnpoc.service.UploadService;

@Controller
@RequestMapping("/upload")
public class UploadController {

    private UploadService uploadService;
    private ScanService scanService;

    @Autowired
    public UploadController(UploadService theUploadService, ScanService theScanService) {
        uploadService = theUploadService;
        scanService = theScanService;
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

            // score immediately; a scoring failure must not fail the upload itself
            try {
                int scored = scanService.scanAll();
                theModel.addAttribute("scanMessage",
                        "Scored " + scored + " customers - see the dashboard.");
            }
            catch (Exception exc) {
                theModel.addAttribute("scanWarning",
                        "Data loaded, but scoring failed: " + ScanErrorMessages.describe(exc)
                        + " Use 'Re-run predictions' on the dashboard to retry.");
            }
        }
        catch (Exception exc) {
            theModel.addAttribute("errorMessage", "Upload failed: " + exc.getMessage());
        }
        return "upload";
    }
}
