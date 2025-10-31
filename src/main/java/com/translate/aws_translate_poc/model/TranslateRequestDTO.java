package com.translate.aws_translate_poc.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.services.translate.model.Formality;

@Data
public class TranslateRequestDTO {

    @NotBlank
    private String text;

    @NotBlank
    private String sourceLanguageCode;

    @NotBlank
    private String targetLanguageCode;

    private Settings settings;

    @Setter
    @Getter
    public static class Settings {
        private Formality formality;
    }
}
