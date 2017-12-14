package me.codz;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.*;

import java.util.Arrays;
import java.util.List;

/**
 * <p>Created with IDEA
 * <p>Author: laudukang
 * <p>Date: 2017-3-1
 * <p>Time: 14:40
 * <p>Version: 1.0
 */
public class Practise {
    private static ClassLoader CLASSLOADER = Thread.currentThread().getContextClassLoader();

    private SparkConf sparkConf;
    private JavaSparkContext sc;

    @Before
    public void initSpark() {
        sparkConf = new SparkConf().setMaster("local[2]").setAppName("Spark Learning Test");
        sc = new JavaSparkContext(sparkConf);
    }

    @After
    public void close() {
        sc.close();
    }

    @Test
    @Ignore
    public void testReduce() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        JavaRDD<Integer> distData = sc.parallelize(data);
        Assert.assertEquals(15, distData.reduce((x, y) -> x + y).intValue());
    }

    @Test
    @Ignore
    public void testStrLength() {
        JavaRDD<String> lines = sc.textFile(CLASSLOADER.getResource("String.txt").getPath());
        JavaRDD<Integer> lineLengths = lines.map(s -> s.length());
        int totalLength = lineLengths.reduce((a, b) -> a + b);
        Assert.assertEquals(7, totalLength);
    }

}
