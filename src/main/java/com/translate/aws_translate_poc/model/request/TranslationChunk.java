package com.translate.aws_translate_poc.model.request;

import java.util.List;

/**
 * Represents a chunk of text to be translated, along with its original indexes.
 */
public class TranslationChunk {

  private final int chunkIndex;
  private final List<Integer> originalIndexes;
  private final String payload;

  public TranslationChunk(int chunkIndex, List<Integer> originalIndexes, String payload) {
    this.chunkIndex = chunkIndex;
    this.originalIndexes = originalIndexes;
    this.payload = payload;
  }

  public int getChunkIndex() {
    return chunkIndex;
  }

  public List<Integer> getOriginalIndexes() {
    return originalIndexes;
  }

  public String getPayload() {
    return payload;
  }
}

