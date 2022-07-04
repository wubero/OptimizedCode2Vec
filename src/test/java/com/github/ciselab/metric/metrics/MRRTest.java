package com.github.ciselab.metric.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.ciselab.support.GenotypeSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class MRRTest {

    @Tag("File")
    @Test
    public void checkNameTest() {
        MRR metric = new MRR(GenotypeSupport.dir_path + "/src/test/resources/testPredictionsWithScore.txt");
        assertEquals("MRR", metric.getName());
    }

    @Tag("File")
    @Test
    public void calculateScoreTest() {
        MRR metric = new MRR(GenotypeSupport.dir_path + "/src/test/resources/testPredictionsWithScore.txt");
        assertEquals(0.75, metric.calculateScore());
    }
}
