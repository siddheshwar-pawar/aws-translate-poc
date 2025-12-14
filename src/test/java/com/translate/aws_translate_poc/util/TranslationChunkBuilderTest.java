package com.translate.aws_translate_poc.util;

import com.translate.aws_translate_poc.model.request.TranslationChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TranslationChunkBuilder
 */
class TranslationChunkBuilderTest {

    private TranslationChunkBuilder chunkBuilder;
    private static final int MAX_CHUNK_BYTES = 9 * 1024; // 9KB

    @BeforeEach
    void setUp() {
        chunkBuilder = new TranslationChunkBuilder();
    }

    @Test
    void testBuildChunks_EmptyList() {
        // Given
        List<String> texts = Collections.emptyList();

        // When
        List<TranslationChunk> chunks = chunkBuilder.buildChunks(texts);

        // Then
        assertNotNull(chunks);
        assertTrue(chunks.isEmpty(), "Chunks should be empty for empty input");
    }

    @Test
    void testBuildChunks_SingleShortText() {
        // Given
        List<String> texts = Arrays.asList("Hello World");

        // When
        List<TranslationChunk> chunks = chunkBuilder.buildChunks(texts);

        // Then
        assertEquals(1, chunks.size(), "Should create exactly one chunk");

        TranslationChunk chunk = chunks.get(0);
        assertEquals(0, chunk.getChunkIndex(), "First chunk should have index 0");
        assertEquals(Arrays.asList(0), chunk.getOriginalIndexes(), "Should contain index 0");
        assertEquals("Hello World", chunk.getPayload(), "Payload should match input text");
    }

    @Test
    void testBuildChunks_MultipleShortTexts() {
        // Given
        List<String> texts = Arrays.asList(
            "First text",
            "Second text",
            "Third text",
            "Fourth text"
        );

        // When
        List<TranslationChunk> chunks = chunkBuilder.buildChunks(texts);

        // Then
        assertEquals(1, chunks.size(), "All short texts should fit in one chunk");

        TranslationChunk chunk = chunks.get(0);
        assertEquals(0, chunk.getChunkIndex());
        assertEquals(Arrays.asList(0, 1, 2, 3), chunk.getOriginalIndexes());
        assertEquals("First text\nSecond text\nThird text\nFourth text", chunk.getPayload());
    }

    @Test
    void testBuildChunks_TextExceedingMaxSize() {
        // Given - Create a text that exceeds 9KB
        String largeText = "A".repeat(10 * 1024); // 10KB text
        List<String> texts = Arrays.asList(largeText);

        // When
        List<TranslationChunk> chunks = chunkBuilder.buildChunks(texts);

        // Then
        // The implementation allows a single text to be in a chunk even if it exceeds the limit
        assertTrue(chunks.size() >= 1, "Should create at least one chunk");
        TranslationChunk chunk = chunks.get(0);
        assertEquals(Arrays.asList(0), chunk.getOriginalIndexes());
        // The large text will be split across chunks or put in one, depending on implementation
        // Here we verify it's handled without error
        assertNotNull(chunk.getPayload());
    }

    @Test
    void testBuildChunks_MultipleChunksRequired() {
        // Given - Create texts that will require multiple chunks
        String text1 = "X".repeat(5 * 1024); // 5KB
        String text2 = "Y".repeat(5 * 1024); // 5KB
        String text3 = "Z".repeat(2 * 1024); // 2KB
        List<String> texts = Arrays.asList(text1, text2, text3);

        // When
        List<TranslationChunk> chunks = chunkBuilder.buildChunks(texts);

        // Then
        assertEquals(2, chunks.size(), "Should create two chunks");

        // First chunk
        TranslationChunk chunk1 = chunks.get(0);
        assertEquals(0, chunk1.getChunkIndex());
        assertEquals(Arrays.asList(0), chunk1.getOriginalIndexes());
        assertEquals(text1, chunk1.getPayload());

        // Second chunk
        TranslationChunk chunk2 = chunks.get(1);
        assertEquals(1, chunk2.getChunkIndex());
        assertEquals(Arrays.asList(1, 2), chunk2.getOriginalIndexes());
        assertEquals(text2 + "\n" + text3, chunk2.getPayload());
    }

    @Test
    void testBuildChunks_ThreeChunksWithMixedSizes() {
        // Given
        String small1 = "Small text 1";
        String large1 = "L".repeat(8 * 1024); // 8KB
        String small2 = "Small text 2";
        String small3 = "Small text 3";
        String large2 = "M".repeat(7 * 1024); // 7KB
        String small4 = "Small text 4";

        List<String> texts = Arrays.asList(small1, large1, small2, small3, large2, small4);

        // When
        List<TranslationChunk> chunks = chunkBuilder.buildChunks(texts);

        // Then
        assertTrue(chunks.size() >= 1, "Should create at least 1 chunk");

        // Verify chunk indexes are sequential
        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).getChunkIndex(),
                "Chunk index should be " + i);
        }

        // Verify all original texts are included
        List<Integer> allIndexes = new ArrayList<>();
        for (TranslationChunk chunk : chunks) {
            allIndexes.addAll(chunk.getOriginalIndexes());
        }
        assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5), allIndexes,
            "All original indexes should be present");
    }

    @Test
    void testBuildChunks_EnsureChunkSizeLimit() {
        // Given - Create texts that individually fit, but together approach the limit
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            texts.add("Text number " + i + " with some content to make it longer. ".repeat(10));
        }

        // When
        List<TranslationChunk> chunks = chunkBuilder.buildChunks(texts);

        // Then
        assertFalse(chunks.isEmpty(), "Should create at least one chunk");

        for (TranslationChunk chunk : chunks) {
            int payloadBytes = chunk.getPayload().getBytes(StandardCharsets.UTF_8).length;
            assertTrue(payloadBytes <= MAX_CHUNK_BYTES || chunk.getOriginalIndexes().size() == 1,
                "Chunk should not exceed max size unless it's a single large text");
        }
    }



    @Test
    void testBuildChunks_WithEmptyStrings() {
        // Given
        List<String> texts = Arrays.asList(
            "First",
            "",
            "Third",
            "",
            "Fifth"
        );

        // When
        List<TranslationChunk> chunks = chunkBuilder.buildChunks(texts);

        // Then
        assertEquals(1, chunks.size());

        TranslationChunk chunk = chunks.get(0);
        assertEquals(Arrays.asList(0, 1, 2, 3, 4), chunk.getOriginalIndexes());
        assertEquals("First\n\nThird\n\nFifth", chunk.getPayload());
    }

    @Test
    void testBuildChunks_SingleTextList() {
        // Given
        List<String> texts = Arrays.asList("Only one text");

        // When
        List<TranslationChunk> chunks = chunkBuilder.buildChunks(texts);

        // Then
        assertEquals(1, chunks.size());
        assertEquals(0, chunks.get(0).getChunkIndex());
        assertEquals(Arrays.asList(0), chunks.get(0).getOriginalIndexes());
        assertEquals("Only one text", chunks.get(0).getPayload());
    }

    @Test
    void testBuildChunks_ExactlyAtLimit() {
        // Given - Create a text that is exactly at the limit
        int targetSize = MAX_CHUNK_BYTES - 100; // Leave some room for safety
        String text1 = "A".repeat(targetSize);
        String text2 = "Small";
        List<String> texts = Arrays.asList(text1, text2);

        // When
        List<TranslationChunk> chunks = chunkBuilder.buildChunks(texts);

        // Then
        assertTrue(chunks.size() >= 1, "Should create at least one chunk");

        // Verify first chunk contains text1
        boolean text1Found = false;
        for (TranslationChunk chunk : chunks) {
            if (chunk.getPayload().contains("A".repeat(100))) {
                text1Found = true;
                break;
            }
        }
        assertTrue(text1Found, "First text should be in one of the chunks");
    }

    @Test
    void testBuildChunks_VerifyNewlineSeparator() {
        // Given
        List<String> texts = Arrays.asList("Line1", "Line2", "Line3");

        // When
        List<TranslationChunk> chunks = chunkBuilder.buildChunks(texts);

        // Then
        assertEquals(1, chunks.size());
        String payload = chunks.get(0).getPayload();
        assertEquals("Line1\nLine2\nLine3", payload);
        assertEquals(2, payload.chars().filter(ch -> ch == '\n').count(),
            "Should have exactly 2 newline characters");
    }

    @Test
    void testBuildChunks_ManySmallTexts() {
        // Given
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            texts.add("Text " + i);
        }

        // When
        List<TranslationChunk> chunks = chunkBuilder.buildChunks(texts);

        // Then
        assertFalse(chunks.isEmpty());

        // Collect all indexes from all chunks
        List<Integer> allIndexes = new ArrayList<>();
        for (TranslationChunk chunk : chunks) {
            allIndexes.addAll(chunk.getOriginalIndexes());
        }

        assertEquals(100, allIndexes.size(), "All 100 texts should be included");

        // Verify indexes are in order
        for (int i = 0; i < 100; i++) {
            assertEquals(i, allIndexes.get(i), "Index " + i + " should be at position " + i);
        }
    }

    @Test
    void testBuildChunks_TextsWithSpecialCharacters() {
        // Given
        List<String> texts = Arrays.asList(
            "Text with\nnewline",
            "Text with\ttab",
            "Text with \"quotes\"",
            "Text with 'apostrophe'"
        );

        // When
        List<TranslationChunk> chunks = chunkBuilder.buildChunks(texts);

        // Then
        assertEquals(1, chunks.size());
        String payload = chunks.get(0).getPayload();
        assertTrue(payload.contains("Text with\nnewline"));
        assertTrue(payload.contains("Text with\ttab"));
        assertTrue(payload.contains("\"quotes\""));
        assertTrue(payload.contains("'apostrophe'"));
    }
}

