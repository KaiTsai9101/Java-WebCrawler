package chapter1.kai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlPool_QuotesToScrape {
    public static void main(String[] args) {
        getUrl("http://quotes.toscrape.com/");
    }

    private static void getUrl(String baseUrl) {
        // 1.创建一个Map来存储已访问的URL
        // key: URL地址, value: 是否已访问过(false=未访问, true=已访问)
        Map<String, Boolean> oldMap = new LinkedHashMap<>();

        // 2.提取域名（用于处理相对路径）
        // html页面中很多路径为相对路径，所以需要记录上一次访问的域名
        String oldLinkHost = "";
        Pattern p = Pattern.compile("(https?://)?[^/\\s]*");
        Matcher m = p.matcher(baseUrl);
        if (m.find()) {
            oldLinkHost = m.group();
        }

        // 3.将起始URL加入待访问列表
        oldMap.put(baseUrl, false);

        // 4.开始爬取
        oldMap = crawlLinks(oldLinkHost, oldMap);

        // 5.输出所有抓取到的链接
        for (Map.Entry<String, Boolean> mapping : oldMap.entrySet()) {
            System.out.println("链接：" + mapping.getKey());
        }
    }

    private static Map<String, Boolean> crawlLinks(String oldLinkHost, Map<String, Boolean> oldMap) {
        // 1.创建新Map存储本轮发现的URL
        Map<String, Boolean> newMap = new LinkedHashMap<>();
        String oldLink = "";

        // 2.遍历所有未访问的URL
        for (Map.Entry<String, Boolean> mapping : oldMap.entrySet()) {
            System.out.println("链接: " + mapping.getKey() + " ----check: " + mapping.getValue());
            if (mapping.getValue()) {
                continue;
            }
            oldLink = mapping.getKey();
            try {
                // 3.发起HTTP请求获取页面内容
                URL url = new URL(oldLink);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    // 添加延迟，避免频繁请求
                    Thread.sleep(500);
                    // 4.用正则表达式提取所有 <a> 标签的链接
                    Pattern p = Pattern.compile("<a\\s*class\\s*=\\s*[\"']tag[\"'].*?href=[\"']?((https?://)?/?[^\"']+)[\"']?.*?>(.+)</a>");
                    Matcher matcher = null;
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        matcher = p.matcher(line);
                        if (matcher.find()) {
                            /*
                            以Pattern p为例：
                            Pattern p = Pattern.compile("<a.*?href=[\"']?((https?://)?/?[^\"']+)[\"']?.*?>(.+)</a>");
                            这里有几个捕获组：按()分组
                            组1：((https?://)?/?[^\"']+) ← 这是 matcher.group(1) 取的内容
                            组2：(https?://)? ← 这是 matcher.group(2)（嵌套在组1内部）
                            组3：(.+) ← 这是链接文本，matcher.group(3)
                             */
                            String newLink = matcher.group(1).trim();
                            // 5.处理相对路径
                            if (!newLink.startsWith("http")) {
                                if (newLink.startsWith("/")) {
                                    newLink = oldLinkHost + newLink;
                                } else {
                                    newLink = oldLinkHost + "/" + newLink;
                                }
                            }
                            // 6.去重并过滤
                            if (newLink.endsWith("/")) {
                                newLink = newLink.substring(0, newLink.length() - 1);
                            }
                            if (!oldMap.containsKey(newLink)
                                    && !newMap.containsKey(newLink)
                                    && newLink.startsWith(oldLinkHost)) {
                                // 7.标记当前URL为待访问列表
                                newMap.put(newLink, false);
                            }
                        }
                    }
                }
            } catch (Exception e) {

            }
            oldMap.replace(oldLink, false, true);
        }
        // 8.递归爬取新发现的链接，并递归爬取
        if (!newMap.isEmpty()) {
            oldMap.putAll(newMap);
            oldMap.putAll(crawlLinks(oldLinkHost, oldMap));
        }
        return oldMap;
    }
}
