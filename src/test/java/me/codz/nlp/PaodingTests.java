package me.codz.nlp;

import net.paoding.analysis.analyzer.PaodingAnalyzer;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.junit.Test;
import scala.Tuple2;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Created with IDEA
 * <p>Author: laudukang
 * <p>Date: 2017-3-2
 * <p>Time: 10:29
 * <p>Version: 1.0
 */
public class PaodingTests {
    private static final Analyzer ANALYZER = new PaodingAnalyzer();
    private static ClassLoader CLASSLOADER = Thread.currentThread().getContextClassLoader();

    @SuppressWarnings("unchecked")
    @Test
    public void testWithSpark() {
        JavaSparkContext sc = new JavaSparkContext("local[2]", "First Spark App");

        int NUMBER_OF_PARTITION = 1;
        JavaRDD<String> textFile = sc.textFile(CLASSLOADER.getResource("nlp-source/Article.txt").getPath(), NUMBER_OF_PARTITION);

        JavaPairRDD<String, Integer> counts = textFile.map(x -> {
            List<String> list = wordAnalyzer(x);
            return list.toString()
                    .replace("[", "")
                    .replace("]", "")
                    .split(",");
        }).flatMap(v -> Arrays.asList(v).iterator())
                .mapToPair((PairFunction<String, String, Integer>) s -> new Tuple2(StringUtils.trim(s), 1))
                .reduceByKey((Function2<Integer, Integer, Integer>) (x, y) -> x + y);

        URL url = CLASSLOADER.getResource("");
        String savePath = new File(url.getFile()).getAbsolutePath() + File.separator + "test-result" + File.separator + "paoding";
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
    public void analysisCompare() throws IOException {
        String text = "你吃饭了吗";

        TokenStream ts = ANALYZER.tokenStream("text", new StringReader(text));
        ts.reset();
        // 添加工具类 注意：以下这些与之前lucene2.x版本不同的地方
        CharTermAttribute offAtt = ts.addAttribute(CharTermAttribute.class);
        // 循环打印出分词的结果，及分词出现的位置
        while (ts.incrementToken()) {
            System.out.print(offAtt.toString() + "\t");
        }
        System.out.println();
        ts.close();
    }


    @Test
    public void testInMemoryShortAnalyzer() {
        try {
//            Directory idx = FSDirectory.open(new File("D:/data/lucene/fix"));
            Directory idx = new RAMDirectory();
            // Make an writer to create the index
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_4, ANALYZER);

            IndexWriter writer = new IndexWriter(idx, iwc);

            // Add some Document objects containing quotes
            writer.addDocument(createDocument("维基百科:关于中文维基百科", "维基百科:关于中文维基百科"));

            writer.commit();

            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(idx));


            ScoreDoc[] result = searcher.search(new QueryParser("title", ANALYZER).parse("title:'维基'"), 10).scoreDocs;

            Arrays.stream(result).forEach(System.out::println);

        } catch (Exception ioe) {
            // In this example we aren't really doing an I/O, so this
            // exception should never actually be thrown.
            ioe.printStackTrace();
        }
    }

    @Test
    public void testSplitText() throws IOException {
        String text = "你吃饭了吗";
        TokenStream ts = ANALYZER.tokenStream("text", new StringReader(text));
        ts.reset();
        // 添加工具类 注意：以下这些与之前lucene2.x版本不同的地方
        CharTermAttribute offAtt = ts.addAttribute(CharTermAttribute.class);

        while (ts.incrementToken()) {
            System.out.print(offAtt.toString() + "\t");
        }

        ts.close();
    }

    @Test
    public void testWordAnalyzer() {
        try {
            List<String> words = wordAnalyzer("我想静静的时候你");

            words.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Document createDocument(String title, String content) {
        Document doc = new Document();

        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));

        return doc;
    }

    private static List<String> wordAnalyzer(String str) throws IOException {
        List<String> list = new ArrayList<>();
        StringReader stringReader = new StringReader(str);
        TokenStream stream = ANALYZER.tokenStream(str, stringReader);
        try {
            //获取词与词之间的位置增量
            PositionIncrementAttribute postiona = stream.addAttribute(PositionIncrementAttribute.class);
            //获取各个单词之间的偏移量
            OffsetAttribute offseta = stream.addAttribute(OffsetAttribute.class);
            //获取每个单词信息
            CharTermAttribute chara = stream.addAttribute(CharTermAttribute.class);
            //获取当前分词的类型
            TypeAttribute typea = stream.addAttribute(TypeAttribute.class);

            stream.reset();
            //Lucene4.6.0之后需要reset，下面close同，不然有异常 TokenStream contract violation: reset()/close()
            //http://lucene.apache.org/core/4_6_0/core/org/apache/lucene/analysis/TokenStream.html

            while (stream.incrementToken()) {
                //System.out.print("位置增量" + postiona.getPositionIncrement() + ":\t");
                //System.out.println(chara + "\t[" + offseta.startOffset() + " - " + offseta.endOffset() + "]\t<" + typea + ">");
                list.add(chara.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stringReader.close();
            stream.end();
            stream.close();
        }
        //list.forEach(System.out::println);
        return list;
    }
}
