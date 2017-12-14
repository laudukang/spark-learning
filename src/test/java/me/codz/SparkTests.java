package me.codz;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;
import scala.Tuple2;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Created with IDEA
 * <p>Author: laudukang
 * <p>Date: 2017-2-28
 * <p>Time: 18:01
 * <p>Version: 1.0
 */
public class SparkTests {

    @Test
    public void testPi() {
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

    @Test
    public void testWordCount() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        JavaSparkContext sc = new JavaSparkContext("local[2]", "First Spark App");
        JavaRDD<String> textFile = sc.textFile(classLoader.getResource("nlp-source/UserPurchaseHistory.csv").getPath());

        JavaPairRDD<String, Integer> counts = textFile
                .flatMap(s -> Arrays.asList(s.split("[, ]")).iterator())
                .mapToPair(word -> new Tuple2<>(word, 1))
                .reduceByKey((a, b) -> a + b);

        URL url = SparkTests.class.getClassLoader().getResource("");

        String savePath = new File(url.getFile()).getAbsolutePath() + File.separator + "test-result" + File.separator + "userpurchasehistoryresult";

        File file = new File(savePath);

        if (file.exists()) {
            Arrays.stream(file.listFiles()).forEach(File::delete);
            file.delete();
        }

        counts.repartition(1).saveAsTextFile(savePath);
    }

    @Test
    public void testSearch() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        SparkConf sparkConf = new SparkConf().setMaster("local[2]").setAppName("First Spark App");

        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        SQLContext sqlContext = new SQLContext(sc);

        JavaRDD<String> textFile = sc.textFile(classLoader.getResource("nlp-source/Search.txt").getPath());

        JavaRDD<Row> rowRDD = textFile.map(RowFactory::create);
        List<StructField> fields = Arrays.asList(
                DataTypes.createStructField("line", DataTypes.StringType, true));
        StructType schema = DataTypes.createStructType(fields);

        Dataset df = sqlContext.createDataFrame(rowRDD, schema);

        Dataset errors = df.filter(df.col("line").like("%ERROR%"));

        System.out.println("total error count:" + errors.count());
        System.out.println("mysql error count" + errors.filter(df.col("line").like("%MySQL%")).count());
        System.out.println("total mysql count:" + df.filter(df.col("line").like("%MySQL%")).count());

        errors.filter(df.col("line").like("%MySQL%")).collectAsList().forEach(System.out::println);
    }
}
