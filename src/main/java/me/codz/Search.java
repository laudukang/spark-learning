package me.codz;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.Arrays;
import java.util.List;

/**
 * <p>Created with IDEA
 * <p>Author: laudukang
 * <p>Date: 2017-3-1
 * <p>Time: 10:41
 * <p>Version: 1.0
 */
public class Search {
	public static void main(String[] args) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		SparkConf sparkConf = new SparkConf().setMaster("local[2]").setAppName("First Spark App");

		JavaSparkContext sc = new JavaSparkContext(sparkConf);
		SQLContext sqlContext = new SQLContext(sc);

		JavaRDD<String> textFile = sc.textFile(classLoader.getResource("test/Search.txt").getPath());

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
