import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

import com.lowagie.text.Paragraph;
import org.jsoup.*;

import net.sourceforge.tess4j.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) throws IOException, AWTException {
        // System.setProperty("jna.library.path", "32".equals(System.getProperty("sun.arch.data.model")) ? "lib/win32-x86" : "lib/win32-x86-64");
        Scanner in = new Scanner(System.in);
        while(true){
            System.out.print("Spam to search");
            if(in.hasNext()){
                System.out.println("Searching...");
                search();
                in.next();
            }
        }
    }

    public static void search() throws IOException, AWTException {
        BufferedImage question = new Robot().createScreenCapture(new Rectangle(10, 300, 850, 975));
        ImageIO.write(question, "png", new File("question.png"));
/*
        BufferedImage answer = new Robot().createScreenCapture(new Rectangle(10, 775, 850, 500));
        ImageIO.write(answer, "png", new File("answer.png"));
*/
        File questionImage = new File("question.png");
        /*
        File answerImage = new File("answer.png");
        */
        ITesseract instance = new Tesseract();  // JNA Interface Mapping

        try {
            String fullString = instance.doOCR(questionImage).replaceAll("\n", " ");
            String questionString = fullString.split("\\?")[0];
            System.out.println("Question: " + questionString);
            String[] answerStrings = fullString.split("\\?")[1].split(" ");

            String[] answerStringsConcated = new String[3];
            int j = 0;
            for(int i = 0; i < answerStrings.length; i++){
                if(!answerStrings[i].equals("")) {
                    answerStringsConcated[j] = answerStrings[i];
                    j++;
                }
            }

            for (String s : answerStringsConcated) {
                System.out.println(s);
            }
            /*
            String answerString = instance.doOCR(answerImage).replaceAll("\n", " ");
            System.out.println("Answers: " + answerString);
            */
/*
            //BingSearch bing = new BingSearch();
            // bing.Search(result);

            URL url = new URL("https://www.google.com/search?q=" + result.replaceAll(" ", "+").replaceAll("\"", ""));
            //System.out.println(url);

            URLConnection urlConnect = url.openConnection();
            urlConnect.addRequestProperty("User-Agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            urlConnect.getInputStream()));
            String inputLine;

            StringBuilder inputSaved = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                inputSaved.append(inputLine);
            in.close();

            //String pattern = "(?i)(<title.*?>)(.+?)()";
            //String updated = inputSaved.toString().replaceAll(pattern, "$2");
            //System.out.println(inputSaved);
            Document doc = Jsoup.parseBodyFragment(inputSaved.toString());
            Elements links = doc.select("a[href]");

            //System.out.println(doc);

            for (int i = 31; i < 40; i++) {
                Element l = links.get(i);
                try {
                    String linkHref = l.attr("href").substring(0, l.attr("href").indexOf("&")).replaceAll("(http:\\/\\/webcache\\.googleusercontent\\.com\\/.*?:.*?:)", "");
                    linkHref = linkHref.replace("/url?q=", "");
                    System.out.println(linkHref);
                    //java.awt.Desktop.getDesktop().browse(new URI(linkHref));
                } catch (StringIndexOutOfBoundsException e) {
                    System.out.println("Nothing");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

*/
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }

    }
}