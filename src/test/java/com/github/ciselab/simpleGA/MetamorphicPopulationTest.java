package com.github.ciselab.simpleGA;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.ciselab.support.ConfigManager;
import com.github.ciselab.support.FileManagement;
import com.github.ciselab.support.GenotypeSupport;
import com.github.ciselab.support.MetricCache;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MetamorphicPopulationTest {

    RandomGenerator r = new SplittableRandom(101010);
    GenotypeSupport genotypeSupport;
    ConfigManager configManager;

    @BeforeEach
    public void setUp() {
        genotypeSupport = new GenotypeSupport(new MetricCache());
        configManager = genotypeSupport.getConfigManager();
    }

    @AfterEach
    public void after() {
        FileManagement.removeOtherDirs(FileManagement.dataDir);
    }

    @Test
    public void createPopulation_getFittest_maximizeTest() {
        int popSize = 3;
        MetamorphicPopulation population = new MetamorphicPopulation(popSize, r, 6, false, genotypeSupport);
        double bestFitness = -1;
        configManager.setMaximize(true);
        for(int i = 0; i < popSize; i++) {
            MetamorphicIndividual individual = new MetamorphicIndividual(genotypeSupport);
            individual.createIndividual(r, 2, 6);
            double fitness = Math.random();
            if(fitness > bestFitness) {
                bestFitness = fitness;
            }
            individual.setFitness(fitness);
            population.saveIndividual(i, individual);
        }
        assertEquals(population.getFittest().getFitness(), bestFitness);
    }

    @Test
    public void createPopulation_getFittest_minimizeTest() {
        int popSize = 3;
        MetamorphicPopulation population = new MetamorphicPopulation(popSize, r, 6, false, genotypeSupport);
        double bestFitness = 10;
        configManager.setMaximize(false);
        for(int i = 0; i < popSize; i++) {
            MetamorphicIndividual individual = new MetamorphicIndividual(genotypeSupport);
            individual.createIndividual(r, 2, 6);
            double fitness = Math.random();
            if(fitness < bestFitness) {
                bestFitness = fitness;
            }
            individual.setFitness(fitness);
            population.saveIndividual(i, individual);
        }
        assertEquals(population.getFittest().getFitness(), bestFitness);
    }

    @Test
    public void createPopulation_withInitializeTest() {
        MetamorphicPopulation population = new MetamorphicPopulation(3, r, 6, true, genotypeSupport);
        assertEquals(population.size(), 3);
        for(int i = 0; i < population.size(); i++) {
            assertNotNull(population.getIndividual(i));
        }
    }

    @Test
    public void populationStringTest() {
        MetamorphicPopulation population = new MetamorphicPopulation(3, r, 6, true, genotypeSupport);
        assertTrue(population.toString().contains("MetamorphicPopulation{"));
    }

}
