package com.translate.aws_translate_poc.model.response;

import java.util.List;
import lombok.Getter;

@Getter
public class TranslatedChunk {

  private final int chunkIndex;
  private final List<Integer> originalIndexes;
  private final List<String> translatedLines;

  public TranslatedChunk(int chunkIndex,
                         List<Integer> originalIndexes,
                         List<String> translatedLines) {
    this.chunkIndex = chunkIndex;
    this.originalIndexes = originalIndexes;
    this.translatedLines = translatedLines;
  }

}
