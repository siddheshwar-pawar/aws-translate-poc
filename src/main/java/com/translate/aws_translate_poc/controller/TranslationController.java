package com.translate.aws_translate_poc.controller;

import com.translate.aws_translate_poc.model.TranslateRequestDTO;
import com.translate.aws_translate_poc.model.TranslateResponseDTO;
import com.translate.aws_translate_poc.service.AmazonTranslateService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/translate")
public class TranslationController {

    private final AmazonTranslateService translateService;

    public TranslationController(AmazonTranslateService translateService) {
        this.translateService = translateService;
    }

    @PostMapping
    public TranslateResponseDTO translate(@Validated @RequestBody TranslateRequestDTO request) {
        return translateService.translateText(request);
    }
}
