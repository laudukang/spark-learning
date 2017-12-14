package me.codz.nlp;

import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.junit.Test;

import java.util.List;

/**
 * <p>Created with IDEA
 * <p>Author: laudukang
 * <p>Date: 2017-3-14
 * <p>Time: 10:32
 * <p>Version: 1.0
 */
public class ApdplatTests {

    @Test
    public void testWordAnalyzer() {
        List<Word> wordList = WordSegmenter.seg("TLC闪存今年占比超75%：Q1季度SSD已涨价10-16%");

        wordList.forEach(System.out::println);
    }
}
