package me.codz;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

/**
 * <p>Created with IDEA
 * <p>Author: laudukang
 * <p>Date: 2017-2-28
 * <p>Time: 18:01
 * <p>Version: 1.0
 */
public class WordCount {
	public static void main(String[] args) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		JavaSparkContext sc = new JavaSparkContext("local[2]", "First Spark App");
		JavaRDD<String> textFile = sc.textFile(classLoader.getResource("UserPurchaseHistory.csv").getPath());

		JavaPairRDD<String, Integer> counts = textFile
				.flatMap(s -> Arrays.asList(s.split("[, ]")).iterator())
				.mapToPair(word -> new Tuple2<>(word, 1))
				.reduceByKey((a, b) -> a + b);

		URL url = WordCount.class.getClassLoader().getResource("");

		String savePath = new File(url.getFile()).getAbsolutePath() + File.separator + "UserPurchaseHistoryResult";
		File file = new File(savePath);

		if (file.exists()) {
			Arrays.stream(file.listFiles()).forEach(File::delete);
			file.delete();
		}

		counts.repartition(1).saveAsTextFile(savePath);
	}
}
