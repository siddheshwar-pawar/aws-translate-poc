package com.translate.aws_translate_poc.service;

import com.translate.aws_translate_poc.model.TranslateRequestDTO;
import com.translate.aws_translate_poc.model.TranslateResponseDTO;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.*;

import java.util.Collections;

@Service
public class AmazonTranslateService {

    private final TranslateClient translateClient;

    public AmazonTranslateService(TranslateClient translateClient) {
        this.translateClient = translateClient;
    }

    public TranslateResponseDTO translateText(TranslateRequestDTO request) {
        TranslateTextRequest.Builder builder = TranslateTextRequest.builder()
                .text(request.getText())
                .sourceLanguageCode(request.getSourceLanguageCode())
                .targetLanguageCode(request.getTargetLanguageCode());

        // Handle optional formality settings
        if (request.getSettings() != null && request.getSettings().getFormality() != null) {
            TranslationSettings settings = TranslationSettings.builder()
                    .formality(request.getSettings().getFormality())
                .build();
            builder.settings(settings);
        }

        TranslateTextResponse response = translateClient.translateText(builder.build());

        TranslateResponseDTO dto = new TranslateResponseDTO();
        dto.setTranslatedText(response.translatedText());
        dto.setSourceLanguageCode(response.sourceLanguageCode());
        dto.setTargetLanguageCode(response.targetLanguageCode());
        dto.setAppliedTerminologies(Collections.emptyList()); // Placeholder
        if (response.appliedSettings() != null && response.appliedSettings().formality() != null) {
            dto.setAppliedSettings(new TranslateResponseDTO.AppliedSettings(response.appliedSettings().formality()));
        }

        return dto;
    }
}
