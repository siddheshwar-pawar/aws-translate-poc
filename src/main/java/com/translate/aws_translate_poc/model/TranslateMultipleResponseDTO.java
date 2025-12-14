package com.translate.aws_translate_poc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.services.translate.model.Formality;

import java.util.List;

@Data
public class TranslateMultipleResponseDTO {
    private List<TranslatedItem> items;
    private String sourceLanguageCode;
    private String targetLanguageCode;
    private AppliedSettings appliedSettings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranslatedItem {
        private String originalText;
        private String translatedText;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    public static class AppliedSettings {
        private Formality formality;
    }
}

