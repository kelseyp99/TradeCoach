package com.workers;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Example program to list links from a URL.
 */
public class ListLinks {
    public static void main(String[] args) throws IOException {
        //Validate.isTrue(args.length == 1, "usage: supply url to fetch");
        //String url = args[0];
        //print("Fetching %s...", url);
 //       String url="http://finance.yahoo.com/q?s=WBA";
   //     Document doc = Jsoup.connect(url).get();
       /// Elements links = doc.select("a[href]");
        //Elements media = doc.select("[src]");
        //Elements imports = doc.select("link[href]");
/*
        print("\nMedia: (%d)", media.size());
        for (Element src : media) {
            if (src.tagName().equals("img"))
                print(" * %s: <%s> %sx%s (%s)",
                        src.tagName(), src.attr("abs:src"), src.attr("width"), src.attr("height"),
                        trim(src.attr("alt"), 20));
            else
                print(" * %s: <%s>", src.tagName(), src.attr("abs:src"));
        }

        print("\nImports: (%d)", imports.size());
        for (Element link : imports) {
            print(" * %s <%s> (%s)", link.tagName(),link.attr("abs:href"), link.attr("rel"));
        }

        print("\nLinks: (%d)", links.size());
        for (Element link : links) {
            print(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));
        }*/
        
//	    doc = Jsoup.connect("http://espn.go.com/mens-college-basketball/conferences/standings/_/id/2/year/2012/acc-conference").get();
        String url="http://finance.yahoo.com/q?s=WBA";
        Document doc = Jsoup.connect(url).get();
        Double betaValue;
	    for (Element table : doc.select("div.yfi_quote_summary")) {
	        for (Element row : table.select("tr")) {
	        	Elements ths = row.select("th");
	            Elements tds = row.select("td");
	         //   if (tds.size() > 0) {
	            if (ths.get(0).text().contains("Beta")) {
	      // (table.ge=="Beta:") {
	           //     System.out.println(ths.get(0).text() + ":" + tds.get(0).text());
	                betaValue=Double.parseDouble(tds.get(0).text());
	                System.out.println(ths.get(0).text() + ":" + tds.get(0).text());

	                
	            }
	        }
	    }
    }
    

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
    }
}
