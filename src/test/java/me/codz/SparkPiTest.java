package me.codz;

import org.apache.spark.api.java.JavaSparkContext;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created with IDEA
 * <p>Author: laudukang
 * <p>Date: 2017-2-28
 * <p>Time: 15:53
 * <p>Version: 1.0
 */
public class SparkPiTest {

    public static void main(String[] args) {
        JavaSparkContext sc = new JavaSparkContext("local[2]", "First Spark App");

        int NUM_SAMPLES = 10;
        List<Integer> l = new ArrayList<>(NUM_SAMPLES);
        for (int i = 0; i < NUM_SAMPLES; i++) {
            l.add(i);
        }

        long count = sc.parallelize(l).filter(i -> {
            double x = Math.random();
            double y = Math.random();
            return x * x + y * y < 1;
        }).count();

        System.out.println("Pi is roughly " + 4.0 * count / NUM_SAMPLES);
    }
}
