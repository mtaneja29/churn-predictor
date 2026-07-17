package com.example.churnpoc.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    // thrown by Spring before our upload code even runs
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleFileTooLarge(RedirectAttributes theRedirectAttributes) {
        theRedirectAttributes.addFlashAttribute("errorMessage",
                "That file is too large - the limit is 20 MB.");
        return "redirect:/upload";
    }
}
