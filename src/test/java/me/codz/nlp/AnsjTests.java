package me.codz.nlp;

import com.google.common.collect.Lists;
import me.codz.SparkWordCountTests;
import org.ansj.domain.Result;
import org.ansj.recognition.impl.StopRecognition;
import org.ansj.splitWord.analysis.*;
import org.apache.commons.lang.StringUtils;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.junit.Test;
import scala.Tuple2;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Created with IDEA
 * <p>Author: laudukang
 * <p>Date: 2017-3-1
 * <p>Time: 15:19
 * <p>Version: 1.0
 */
public class AnsjTests {

    private static ClassLoader CLASSLOADER = Thread.currentThread().getContextClassLoader();
    private static StopRecognition FILTER;

    static {
        FILTER = new StopRecognition();
        //FILTER.insertStopNatures("我"); //过滤单词
        //FILTER.insertStopRegexes("小.*?"); //支持正则表达式
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRunWithSpark() {
        JavaSparkContext sc = new JavaSparkContext("local[2]", "First Spark App");

        int NUMBER_OF_PARTITION = 1;
        JavaRDD<String> textFile = sc.textFile(CLASSLOADER.getResource("nlp-source/Article.txt").getPath(),
                NUMBER_OF_PARTITION);

        JavaPairRDD<String, Integer> counts = textFile.map(x -> {
            //just similar with Paoding
            List<String> list = wordAnalyzer(x);
            return list.toString()
                    .replace("[", "")
                    .replace("]", "")
                    .split(",");
        }).flatMap(v -> Arrays.asList(v).iterator())
                .mapToPair((PairFunction<String, String, Integer>) s -> new Tuple2(StringUtils.trim(s), 1))
                .reduceByKey((Function2<Integer, Integer, Integer>) (x, y) -> x + y);

        URL url = SparkWordCountTests.class.getClassLoader().getResource("");
        String savePath = new File(url.getFile()).getAbsolutePath() + File.separator + "test-result" + File.separator + "ansj";
        File file = new File(savePath);
        if (file.exists()) {
            Arrays.stream(file.listFiles()).forEach(File::delete);
            file.delete();
        }
        counts.saveAsTextFile(savePath);

        //List<Tuple2<String, Integer>> output = counts.collect();
        //output.stream().forEach(t -> System.out.println(t._1()+"("+t._2()+")"));

        sc.stop();
    }

    @Test
    public void testAnsj() {
        String str = "洁面仪配合洁面深层清洁毛孔 清洁鼻孔面膜碎觉使劲挤才能出一点点皱纹 脸颊毛孔修复的看不见啦 " +
                "草莓鼻历史遗留问题没辙 脸和脖子差不多颜色的皮肤才是健康的 长期使用安全健康的比同龄人显小五到十岁 " +
                "28岁的妹子看看你们的鱼尾纹";

        //词性标注规范参见：https://github.com/NLPchina/ansj_seg/wiki/%E8%AF%8D%E6%80%A7%E6%A0%87%E6%B3%A8%E8%A7%84%E8%8C%83

        System.out.println(BaseAnalysis.parse(str));
        System.out.println(ToAnalysis.parse(str));
        System.out.println(DicAnalysis.parse(str));
        System.out.println(IndexAnalysis.parse(str));
        System.out.println(NlpAnalysis.parse(str));
    }

    private static List<String> wordAnalyzer(String str) {
        List<String> list = Lists.newArrayList();

        Result result = ToAnalysis.parse(str).recognition(FILTER);
        result.getTerms().forEach(t -> list.add(t.getName()));

        return list;
    }
}
