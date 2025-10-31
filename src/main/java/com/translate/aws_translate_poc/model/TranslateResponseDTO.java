package com.translate.aws_translate_poc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.services.translate.model.Formality;

import java.util.List;

@Data
public class TranslateResponseDTO {
    private String translatedText;
    private String sourceLanguageCode;
    private String targetLanguageCode;
    private List<String> appliedTerminologies;
    private AppliedSettings appliedSettings;

    @Setter
    @Getter
    @AllArgsConstructor
    public static class AppliedSettings {
        private String formality;

        public AppliedSettings(Formality formality) {
        }
    }
}
