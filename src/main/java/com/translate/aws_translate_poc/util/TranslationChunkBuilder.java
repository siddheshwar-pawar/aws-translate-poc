package com.translate.aws_translate_poc.util;

import com.translate.aws_translate_poc.model.request.TranslationChunk;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Utility class to build translation chunks from a list of texts,
 * ensuring that each chunk does not exceed the maximum byte size limit.
 */
@Component
public class TranslationChunkBuilder {

  private static final int MAX_CHUNK_BYTES = 9 * 1024;
  private static final String SEPARATOR = "\n";

  public List<TranslationChunk> buildChunks(List<String> texts) {
    List<TranslationChunk> chunks = new ArrayList<>();
    int chunkIndex = 0;
    StringBuilder currentPayload = new StringBuilder();
    List<Integer> currentIndexes = new ArrayList<>();
    for (int i = 0; i < texts.size(); i++) {
      String text = texts.get(i);
      String candidate = currentPayload.isEmpty()
          ? text
          : currentPayload + SEPARATOR + text;
      int candidateBytes = candidate.getBytes(StandardCharsets.UTF_8).length;
      if (candidateBytes > MAX_CHUNK_BYTES) {
        chunks.add(new TranslationChunk(
            chunkIndex++,
            List.copyOf(currentIndexes),
            currentPayload.toString()
        ));

        currentPayload.setLength(0);
        currentIndexes.clear();
      }

      if (!currentPayload.isEmpty()) {
        currentPayload.append(SEPARATOR);
      }

      currentPayload.append(text);
      currentIndexes.add(i);
    }

    if (!currentPayload.isEmpty()) {
      chunks.add(new TranslationChunk(
          chunkIndex,
          List.copyOf(currentIndexes),
          currentPayload.toString()
      ));
    }
    return chunks;
  }
}
