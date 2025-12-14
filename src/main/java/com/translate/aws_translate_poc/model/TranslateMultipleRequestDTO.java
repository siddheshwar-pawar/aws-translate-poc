package com.translate.aws_translate_poc.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.services.translate.model.Formality;

@Data
public class TranslateMultipleRequestDTO {

  @NotEmpty(message = "texts array cannot be empty")
  private List<String> texts;

  @NotBlank(message = "sourceLanguageCode cannot be blank")
  private String sourceLanguageCode;

  @NotBlank(message = "targetLanguageCode cannot be blank")
  private String targetLanguageCode;

  private Settings settings;

  @Setter
  @Getter
  public static class Settings {
    private Formality formality;
  }
}

