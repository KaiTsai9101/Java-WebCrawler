package chapter1.kai;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class imageCrawl {

    private static String url = "http://www.nipic.com/topic/show_27202_1.html";

    public static void main(String[] args) throws IOException {
        // 方法一：使用apache的HttpClient
        // apacheHttpClient();
        // 方法二：使用Jsoup
        Document document = Jsoup.connect(url).get();
        Elements elements = document.select("li.new-search-works-item");
        for (int i = 0; i < elements.size(); i++) {
            Elements imgElement = elements.get(i).select("a > img");
            String imgUrl = imgElement.attr("src");

            // 处理协议相对URL（以//开头的URL）
            if (imgUrl.startsWith("//")) {
                imgUrl = "https:" + imgUrl;
            } else if (imgUrl.startsWith("/")) {
                // 处理绝对路径
                imgUrl = "https://www.nipic.com" + imgUrl;
            }

            Connection.Response response = Jsoup.connect(imgUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36 Edg/148.0.0.0")
                    .ignoreContentType(true)
                    .execute();
            String name = imgElement.attr("alt");
            ByteArrayInputStream stream = new ByteArrayInputStream(response.bodyAsBytes());
            FileUtils.copyInputStreamToFile(stream, new File("F://pachong//fileItem//" + name + ".png"));

            // 添加延迟，避免频繁请求
            try {
                Thread.sleep(1000); // 每次请求间隔1秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void apacheHttpClient() {
        HttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36 Edg/148.0.0.0");

        HttpResponse response = null;
        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            System.out.println(EntityUtils.toString(entity));
        } catch (Exception e) {

        } finally {

        }
    }
}
