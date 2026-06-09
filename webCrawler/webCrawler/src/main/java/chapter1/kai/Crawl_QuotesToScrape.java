package chapter1.kai;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Crawl_QuotesToScrape {

    private static String url = "http://quotes.toscrape.com";

    public static void main(String[] args) throws IOException {
        Document document = Jsoup.connect(url).get();
        Elements elements = document.select("div.quote");
        for (int i = 0; i < elements.size(); i++) {
            // 提取名言文本
            Elements textElement = elements.get(i).select("span.text");
            String quoteText = textElement.text();

            // 提取作者
            Elements authorElement = elements.get(i).select("small.author");
            String author = authorElement.text();

            // 提取标签
            Elements tagElements = elements.get(i).select("div.tags a.tag");
            StringBuilder tags = new StringBuilder();
            for (int j = 0; j < tagElements.size(); j++) {
                tags.append(tagElements.get(j).text());
                if (j < tagElements.size() - 1) {
                    tags.append(", ");
                }
            }

            // 输出到控制台
            System.out.println("=== 名言 " + (i + 1) + " ===");
            System.out.println("内容: " + quoteText);
            System.out.println("作者: " + author);
            System.out.println("标签: " + tags);
            System.out.println();

            // 保存到文件
            // 创建 downloads 文件夹
            String saveDir = "F:/webCrawler/textItem/";
            Files.createDirectories(Paths.get(saveDir));

            String fileName = saveDir + "quote_" + (i + 1) + ".txt";
            String content = "名言: " + quoteText + "\n" +
                    "作者: " + author + "\n" +
                    "标签: " + tags + "\n\n";
            Files.write(
                    java.nio.file.Paths.get(fileName),
                    content.getBytes("UTF-8")
            );

            // 添加延迟，避免频繁请求
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
