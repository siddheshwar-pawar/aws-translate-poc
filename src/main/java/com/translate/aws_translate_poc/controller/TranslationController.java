package com.translate.aws_translate_poc.controller;

import com.translate.aws_translate_poc.model.TranslateRequestDTO;
import com.translate.aws_translate_poc.model.TranslateResponseDTO;
import com.translate.aws_translate_poc.model.TranslateMultipleRequestDTO;
import com.translate.aws_translate_poc.model.TranslateMultipleResponseDTO;
import com.translate.aws_translate_poc.service.AmazonTranslateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/translate")
public class TranslationController {

    private final AmazonTranslateService translateService;

    public TranslationController(AmazonTranslateService translateService) {
        this.translateService = translateService;
    }

    @PostMapping
    public TranslateResponseDTO translate(@Validated @RequestBody TranslateRequestDTO request) {
        log.info(request.toString());
        return translateService.translateText(request);
    }

    @PostMapping("/batch")
    public TranslateMultipleResponseDTO translateMultiple(@Validated @RequestBody TranslateMultipleRequestDTO request) {
        log.info("Batch translation request: {}", request.toString());
        return translateService.translateMultipleTexts(request);
    }
}
