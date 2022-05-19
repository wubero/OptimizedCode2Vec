package com.github.ciselab.simpleGA;

import com.github.ciselab.lampion.core.transformations.transformers.AddNeutralElementTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.AddUnusedVariableTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.BaseTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.IfFalseElseTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.IfTrueTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.LambdaIdentityTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.RandomParameterNameTransformer;
import com.github.ciselab.lampion.core.transformations.transformers.RenameVariableTransformer;
import com.github.ciselab.metric.Metric;
import com.github.ciselab.metric.metrics.InputLength;
import com.github.ciselab.support.GenotypeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates a metamorphic individual for the metamorphic population.
 * It can increase the size or change the genotype of this metamorphic individual.
 */
public class MetamorphicIndividual {

    private static Logger logger = LogManager.getLogger(MetamorphicIndividual.class);
    private int length = 0;
    private List<BaseTransformer> transformers = new ArrayList<>();
    private double fitness = -1;
    private double[] metrics = new double[GenotypeSupport.getActiveMetrics()];

    /**
     * Create a new metamorphic individual.
     * @param r the random generator used for this run.
     * @param length the length.
     * @param maxTransformerValue the maximum transformer key value.
     */
    public void createIndividual(RandomGenerator r, int length, int maxTransformerValue) {
        transformers.clear();
        metrics = new double[GenotypeSupport.getActiveMetrics()];
        this.length = length;
        for(int i = 0; i < length; i++) {
            int key = r.nextInt(0, maxTransformerValue+1);
            int seed = r.nextInt();
            transformers.add(createTransformers(key, seed));
        }
    }

    /**
     * Get the length of this metamorphic individual.
     * @return the length.
     */
    public int getLength() {
        return length;
    }

    /**
     * Get the transformer at an index.
     * @param index the index.
     * @return the transformer.
     */
    public BaseTransformer getGene(int index) {
        return transformers.get(index);
    }

    /**
     * Get the transformers for this metamorphic individual.
     * @return the list of transformers.
     */
    public List<BaseTransformer> getTransformers() {
        return transformers;
    }

    /**
     * Change the transformer at an index.
     * @param index the index.
     * @param gene the transformer.
     */
    public void setGene(int index, BaseTransformer gene) {
        transformers.set(index, gene);
        length = transformers.size();
        fitness = -1;
    }

    /**
     * Add a new transformer to the list and set the fitness back to -1;
     * @param gene the transformer to add.
     */
    public void addGene(BaseTransformer gene) {
        transformers.add(gene);
        length++;
        fitness = -1;
    }

    /**
     * If the length is not max, increase the length of this metamorphic individual by one.
     * @param maxGeneLength the max length for the transformer list.
     * @param randomGen the random generator used for this run.
     * @param maxValue the maximum value for the transformer keys.
     */
    public void increase(int maxGeneLength, RandomGenerator randomGen, int maxValue) {
        if(length < maxGeneLength) {
            int key = randomGen.nextInt(1, maxValue+1);
            int seed = randomGen.nextInt();
            BaseTransformer newTransformer = createTransformers(key, seed);
            length++;
            fitness = -1;
            if(GenotypeSupport.getDir(transformers).isPresent()) {
                List<BaseTransformer> t = new ArrayList<>();
                t.add(newTransformer);
                String oldDir = GenotypeSupport.getDir(transformers).get() + "/test";
                String name = GenotypeSupport.runTransformations(t, oldDir);
                GenotypeSupport.runCode2vec(name);
                double[] primary = calculateMetric();
                double[] secondary = secondaryMetrics(name);
                int j = 0;
                for(int i = 0; i < metrics.length; i++) {
                    if(i < primary.length)
                        metrics[i] = primary[i];
                    else {
                        metrics[i] = secondary[j];
                        j++;
                    }
                }
                fitness = calculateFitness(metrics);
                transformers.add(newTransformer);
                GenotypeSupport.storeFiles(transformers, name, metrics);
            } else {
                transformers.add(newTransformer);
                if (GenotypeSupport.getMetricResult(transformers).isPresent()) {
                    metrics = GenotypeSupport.getMetricResult(transformers).get();
                    fitness = calculateFitness(metrics);
                }
            }
            logger.info("The gene " + this.hashCode() + " has increased its size to " + this.length);
        }
    }

    /**
     * Decrease the amount of transformers for this metamorphic individual.
     * @param randomGen the random generator used for this run.
     */
    public void decrease(RandomGenerator randomGen) {
        if(length > 1) {
            int drop = randomGen.nextInt(0, length);
            transformers.remove(drop);
            length--;
            logger.info("The gene " + this.hashCode() + " has decreased its size to " + this.length);
            if(GenotypeSupport.getMetricResult(transformers).isPresent()) {
                metrics = GenotypeSupport.getMetricResult(transformers).get();
                fitness = calculateFitness(metrics);
            } else {
                fitness = -1;
                metrics = new double[GenotypeSupport.getActiveMetrics()];
            }
        }
    }

    /**
     * Create a new gene, transformer, for the metamorphic individual.
     * @param key the key for the transformer.
     * @param random the random generator used for this run.
     * @return the transformer created.
     */
    public BaseTransformer createGene(int key, RandomGenerator random) {
        return createTransformers(key, random.nextInt());
    }

    /**
     * Get the fitness of this metamorphic individual. If it does not exist calculate it.
     * @return the fitness of this metamorphic individual.
     */
    public double getFitness() {
        if (fitness < 0.0) {
            String name = GenotypeSupport.runTransformations(transformers, GenotypeSupport.getCurrentDataset());
            GenotypeSupport.runCode2vec(name);
            double[] primary = calculateMetric();
            double[] secondary = secondaryMetrics(name);
            int j = 0;
            for(int i = 0; i < metrics.length; i++) {
                if(i < primary.length)
                    metrics[i] = primary[i];
                else {
                    metrics[i] = secondary[j];
                    j++;
                }
            }
            fitness = calculateFitness(metrics);
            GenotypeSupport.fillFitness(transformers, metrics);
        }
        logger.info("The gene " + this.hashCode() + " has calculated its fitness, it is: " + fitness);
        return fitness;
    }

    /**
     * Getter for the metrics field.
     * @return the metrics array.
     */
    public double[] getMetrics() {
        return metrics;
    }

    /**
     * Set the metrics of this metamorphic individual.
     * @param metrics the metrics to set.
     */
    public void setMetrics(double[] metrics) {
        this.metrics = metrics;
        fitness = calculateFitness(metrics);
    }

    /**
     * Set the fitness of this metamorphic individual.
     * @param fitness the fitness to set.
     */
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    /**
     * Calculate secondary metrics for Pareto front.
     * @param dataset the dataset.
     * @return the array with secondary metric scores.
     */
    private double[] secondaryMetrics(String dataset) {
        List<Metric> metrics = GenotypeSupport.getSecondaryMetrics();
        double[] scores = new double[metrics.size()];
        for(int i = 0; i < metrics.size(); i++) {
            double score = 0;
            if(metrics.get(i).getName().contains("Input_length")) {
                ((InputLength)metrics.get(i)).setDataSet(dataset);
                score = metrics.get(i).CalculateScore();
            } else if(metrics.get(i).getName().contains("Number_of_transformations"))
                score = length;
            scores[i] = score;
        }
        return scores;
    }

    /**
     * Calculate the scores for each metric.
     * @return a list of scores.
     */
    private double[] calculateMetric() {
        List<Metric> metrics = GenotypeSupport.getMetrics();
        double[] scores = new double[metrics.size()];
        for(int i = 0; i < metrics.size(); i++) {
            double score = metrics.get(i).CalculateScore();
            scores[i] = score;
        }
        return scores;
    }

    /**
     * Calculate the global fitness of the metrics with the weights for each metric.
     * @param metrics the list of metrics.
     * @return The global fitness.
     */
    private double calculateFitness(double[] metrics) {
        List<Float> weights = GenotypeSupport.getWeights();
        double output = 0;
        for(int i = 0; i < weights.size(); i++) {
            output += metrics[i]*weights.get(i);
        }
        return output;
    }

    /**
     * Create the transformer based on the key and seed specified.
     * @param key the transformer key.
     * @param seed the transformer seed.
     * @return a transformer that extends the BaseTransformer.
     */
    private BaseTransformer createTransformers(Integer key, Integer seed) {
        switch (key) {
            case 0:
                return new IfTrueTransformer(seed);
            case 1:
                return new IfFalseElseTransformer(seed);
            case 2:
                return new RenameVariableTransformer(seed);
            case 3:
                return new AddNeutralElementTransformer(seed);
            case 4:
                return new AddUnusedVariableTransformer(seed);
            case 5:
                return new RandomParameterNameTransformer(seed);
            case 6:
                return new LambdaIdentityTransformer(seed);
            default:
                logger.error("The key provided does not match a transformer");
                throw new IllegalArgumentException("The key provided does not match a transformer.");
        }
    }

    @Override
    public String toString() {
        String geneString = "[";
        for (BaseTransformer i: transformers) {
            String[] temp = i.getClass().toString().split("\\.");
            String addition = temp[temp.length-1];
            geneString += addition + ", ";
        }
        if(geneString.length() < 5) {
            return "[]";
        } else {
            return geneString.substring(0, geneString.length() - 2) + "]";
        }
    }
}
