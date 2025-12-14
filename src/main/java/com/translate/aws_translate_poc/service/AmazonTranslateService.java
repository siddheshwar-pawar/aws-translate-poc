package com.translate.aws_translate_poc.service;

import com.translate.aws_translate_poc.model.TranslateMultipleRequestDTO;
import com.translate.aws_translate_poc.model.TranslateMultipleResponseDTO;
import com.translate.aws_translate_poc.model.TranslateRequestDTO;
import com.translate.aws_translate_poc.model.TranslateResponseDTO;
import com.translate.aws_translate_poc.model.request.TranslationChunk;
import com.translate.aws_translate_poc.model.response.TranslatedChunk;
import com.translate.aws_translate_poc.util.TranslationChunkBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.Formality;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;
import software.amazon.awssdk.services.translate.model.TranslationSettings;

@Slf4j
@Service
public class AmazonTranslateService {

    @Autowired
    private final TranslationChunkBuilder chunkBuilder;

    private final TranslateClient translateClient;

    public AmazonTranslateService(TranslationChunkBuilder chunkBuilder, TranslateClient translateClient) {
      this.chunkBuilder = chunkBuilder;
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


  public TranslateMultipleResponseDTO translateMultipleTexts(
      TranslateMultipleRequestDTO request) {

    List<String> texts = request.getTexts();

    List<TranslationChunk> chunks =
        chunkBuilder.buildChunks(texts);

    List<TranslatedChunk> translatedChunks = new ArrayList<>();

    Formality appliedFormality = null;
    for (TranslationChunk chunk : chunks) {
      TranslatedChunk translatedChunk =
          translateChunk(chunk, request);
      translatedChunks.add(translatedChunk);
    }

    // Rebuild response in original order
    TranslateMultipleResponseDTO responseDTO =
        rebuildResponse(texts.size(), translatedChunks, request);

    if (appliedFormality != null) {
      responseDTO.setAppliedSettings(
          new TranslateMultipleResponseDTO.AppliedSettings(appliedFormality)
      );
    }

    return responseDTO;
  }



  private TranslateMultipleResponseDTO rebuildResponse(
      int totalSize,
      List<TranslatedChunk> chunks,
      TranslateMultipleRequestDTO request) {

    TranslateMultipleResponseDTO dto =
        new TranslateMultipleResponseDTO();

    List<TranslateMultipleResponseDTO.TranslatedItem> items =
        new ArrayList<>(Collections.nCopies(totalSize, null));

    for (TranslatedChunk chunk : chunks) {
      List<Integer> indexes = chunk.getOriginalIndexes();
      List<String> lines = chunk.getTranslatedLines();

      for (int i = 0; i < indexes.size(); i++) {
        int originalIndex = indexes.get(i);

        TranslateMultipleResponseDTO.TranslatedItem item =
            new TranslateMultipleResponseDTO.TranslatedItem();
        item.setOriginalText(request.getTexts().get(originalIndex));
        item.setTranslatedText(lines.get(i));

        items.set(originalIndex, item);
      }
    }

    dto.setItems(items);
    dto.setSourceLanguageCode(request.getSourceLanguageCode());
    dto.setTargetLanguageCode(request.getTargetLanguageCode());

    return dto;
  }



  private TranslatedChunk translateChunk(
      TranslationChunk chunk,
      TranslateMultipleRequestDTO request) {

    TranslateTextRequest.Builder builder = TranslateTextRequest.builder()
        .text(chunk.getPayload())
        .sourceLanguageCode(request.getSourceLanguageCode())
        .targetLanguageCode(request.getTargetLanguageCode());
    if (request.getSettings() != null && request.getSettings().getFormality() != null) {
      builder.settings(
          TranslationSettings.builder()
              .formality(request.getSettings().getFormality())
              .build()
      );
    }
    TranslateTextResponse response =
        translateClient.translateText(builder.build());
    List<String> translatedLines =
        List.of(response.translatedText().split("\n", -1));
    return new TranslatedChunk(
        chunk.getChunkIndex(),
        chunk.getOriginalIndexes(),
        translatedLines
    );
  }









//    public TranslateMultipleResponseDTO translateMultipleTexts(TranslateMultipleRequestDTO request) {
//        log.info("Translating {} texts from {} to {}", request.getTexts().size(),
//                request.getSourceLanguageCode(), request.getTargetLanguageCode());
//        List<TranslateMultipleResponseDTO.TranslatedItem> translatedItems = new ArrayList<>();
//        Formality appliedFormality = null;
//        for (String text : request.getTexts()) {
//            TranslateTextRequest.Builder builder = TranslateTextRequest.builder()
//                    .text(text)
//                    .sourceLanguageCode(request.getSourceLanguageCode())
//                    .targetLanguageCode(request.getTargetLanguageCode());
//
//            // Handle optional formality settings
//            if (request.getSettings() != null && request.getSettings().getFormality() != null) {
//                TranslationSettings settings = TranslationSettings.builder()
//                        .formality(request.getSettings().getFormality())
//                        .build();
//                builder.settings(settings);
//            }
//
//            TranslateTextResponse response = translateClient.translateText(builder.build());
//
//            // Create translated item
//            TranslateMultipleResponseDTO.TranslatedItem item = new TranslateMultipleResponseDTO.TranslatedItem();
//            item.setOriginalText(text);
//            item.setTranslatedText(response.translatedText());
//            translatedItems.add(item);
//
//            // Capture applied formality from first response
//            if (appliedFormality == null && response.appliedSettings() != null &&
//                    response.appliedSettings().formality() != null) {
//                appliedFormality = response.appliedSettings().formality();
//            }
//        }
//
//        TranslateMultipleResponseDTO dto = new TranslateMultipleResponseDTO();
//        dto.setItems(translatedItems);
//        dto.setSourceLanguageCode(request.getSourceLanguageCode());
//        dto.setTargetLanguageCode(request.getTargetLanguageCode());
//
//        if (appliedFormality != null) {
//            dto.setAppliedSettings(new TranslateMultipleResponseDTO.AppliedSettings(appliedFormality));
//        }
//        return dto;
//    }
}
