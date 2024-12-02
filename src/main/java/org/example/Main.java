package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.util.*;

public class Main {
    static final int MAX_CAPACITY = 150;
    static final int NUM_ITEMS = 100;
    static final int POPULATION_SIZE = 100;
    static final int MAX_ITERATIONS = 1000;
    static final double MUTATION_RATE = 0.05;
    static final int LOG_STEP = 20;

    static class Item {
        int weight;
        int value;

        public Item(int weight, int value) {
            this.weight = weight;
            this.value = value;
        }
    }

    static class Individual {
        boolean[] genes;
        int fitness;

        public Individual(int numGenes) {
            genes = new boolean[numGenes];
        }

        void evaluateFitness(List<Item> items) {
            int totalWeight = 0, totalValue = 0;
            for (int i = 0; i < genes.length; i++) {
                if (genes[i]) {
                    totalWeight += items.get(i).weight;
                    totalValue += items.get(i).value;
                }
            }
            fitness = (totalWeight <= MAX_CAPACITY) ? totalValue : 0;
        }
    }

    public static void main(String[] args) {
        Random random = new Random();
        List<Item> items = generateItems(random);

        List<Individual> population = initializePopulation(random, items.size());
        List<Integer> fitnessHistory = new ArrayList<>();

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            for (Individual individual : population) {
                individual.evaluateFitness(items);
            }

            population.sort(Comparator.comparingInt(ind -> -ind.fitness));
            if (iteration % LOG_STEP == 0) {
                fitnessHistory.add(population.get(0).fitness);
                System.out.println("Iteration " + iteration + ": Best Fitness = " + population.get(0).fitness);
            }

            List<Individual> nextGeneration = new ArrayList<>();
            for (int i = 0; i < POPULATION_SIZE / 2; i++) {
                Individual parent1 = selectParent(population, random);
                Individual parent2 = selectParent(population, random);

                Individual child1 = new Individual(items.size());
                Individual child2 = new Individual(items.size());
                uniformCrossover(parent1, parent2, child1, child2, random);

                mutate(child1, random);
                mutate(child2, random);

                localImprovement(child1, items, random);
                localImprovement(child2, items, random);

                child1.evaluateFitness(items);
                child2.evaluateFitness(items);

                nextGeneration.add(child1);
                nextGeneration.add(child2);
            }
            population = nextGeneration;
        }

        System.out.println("Final Best Fitness: " + population.get(0).fitness);
        plotFitnessGraph(fitnessHistory);
    }

    static List<Item> generateItems(Random random) {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < NUM_ITEMS; i++) {
            items.add(new Item(random.nextInt(5) + 1, random.nextInt(9) + 2));
        }
        return items;
    }

    static List<Individual> initializePopulation(Random random, int numGenes) {
        List<Individual> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Individual individual = new Individual(numGenes);
            for (int j = 0; j < numGenes; j++) {
                individual.genes[j] = random.nextBoolean();
            }
            population.add(individual);
        }
        return population;
    }

    static Individual selectParent(List<Individual> population, Random random) {
        return population.get(random.nextInt(POPULATION_SIZE / 2));
    }

    static void uniformCrossover(Individual parent1, Individual parent2, Individual child1, Individual child2, Random random) {
        for (int i = 0; i < parent1.genes.length; i++) {
            if (random.nextBoolean()) {
                child1.genes[i] = parent1.genes[i];
                child2.genes[i] = parent2.genes[i];
            } else {
                child1.genes[i] = parent2.genes[i];
                child2.genes[i] = parent1.genes[i];
            }
        }
    }

    static void mutate(Individual individual, Random random) {
        for (int i = 0; i < individual.genes.length; i++) {
            if (random.nextDouble() < MUTATION_RATE) {
                individual.genes[i] = !individual.genes[i];
            }
        }
    }

    static void localImprovement(Individual individual, List<Item> items, Random random) {
        for (int i = 0; i < items.size(); i++) {
            if (!individual.genes[i]) continue;
            individual.genes[i] = false;
            individual.evaluateFitness(items);
            if (individual.fitness == 0 || random.nextBoolean()) {
                individual.genes[i] = true;
            }
        }
    }

    static void plotFitnessGraph(List<Integer> fitnessHistory) {
        XYSeries series = new XYSeries("Fitness Progress");

        // Додаємо значення до графіка
        for (int i = 0; i < fitnessHistory.size(); i++) {
            series.add(i * LOG_STEP, fitnessHistory.get(i)); // Використовуємо i без множення на LOG_STEP
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Fitness Progress",
                "Iteration",
                "Fitness",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        JFrame frame = new JFrame("Fitness Progress Chart");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        ChartPanel chartPanel = new ChartPanel(chart);
        frame.add(chartPanel);
        frame.setVisible(true);
    }
}
