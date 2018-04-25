import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        BufferedImage question = new Robot().createScreenCapture(new Rectangle(10, 300, 700, 975));
        ImageIO.write(question, "png", new File("question.png"));

        File questionImage = new File("question.png");
        ITesseract instance = new Tesseract();  // JNA Interface Mapping

        try {
            String fullString = instance.doOCR(questionImage);
            String questionString = (fullString.split("\\?")[0] + "?").replaceAll("\n", " ");
            System.out.println("Question: " + questionString);
            String[] answerStrings = fullString.split("\\?")[1].split("\n");

            String[] answerStringsConcated = new String[3];

            int j = 0;
            for(int i = 0; i < answerStrings.length; i++){
                if(!answerStrings[i].equals("")) {
                    if(j == 3) break;
                    answerStringsConcated[j] = answerStrings[i];
                    j++;
                }
            }

            System.out.println("Possible Answers: ");
            for (String s : answerStringsConcated) {
                System.out.println(s);
            }

            URL url = new URL(("https://www.googleapis.com/customsearch/v1?q=" + questionString.replaceAll(" ", "+").replaceAll("\"", "").split("which")[0] + "?" +
                        "&cx=016409237003735062340%3Anfsrbopx80k" +
                        "&orTerms=" + answerStringsConcated[0].replaceAll(" ", "+") + "," + answerStringsConcated[1].replaceAll(" ", "+") + "," + answerStringsConcated[2].replaceAll(" ", "+") +
                        "&key=AIzaSyBMsx3u8GCtyYT1akAv0zRNiQuxuxQJt1I").replaceAll(" ", "%20"));

            System.out.println(url);

            URLConnection urlConnect = url.openConnection();
            urlConnect.addRequestProperty("User-Agent",
                    "Mozilla");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            urlConnect.getInputStream()));
            String inputLine;

            StringBuilder inputSaved = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                inputSaved.append(inputLine);
            in.close();

            //Start count

            int highest = 0;
            int whichHigh = 0;

            Pattern p = Pattern.compile(answerStringsConcated[0].toLowerCase());
            Matcher m = p.matcher(inputSaved.toString().toLowerCase());
            int i = 0;
            while (m.find()) {
                i++;
            }
            p = Pattern.compile(answerStringsConcated[0].toLowerCase().substring(0, answerStringsConcated[0].length()-1));
            m = p.matcher(inputSaved.toString().toLowerCase());
            while (m.find()) {
                i++;
            }
            if(i >= highest){
                whichHigh = 0;
                highest = i;
            }
            System.out.println("Answer 1 instances: " + i);

            p = Pattern.compile(answerStringsConcated[1].toLowerCase());
            m = p.matcher(inputSaved.toString().toLowerCase());
            i = 0;
            while (m.find()) {
                i++;
            }
            p = Pattern.compile(answerStringsConcated[1].toLowerCase().substring(0, answerStringsConcated[1].length()-1));
            m = p.matcher(inputSaved.toString().toLowerCase());
            while (m.find()) {
                i++;
            }
            if(i >= highest){
                whichHigh = 1;
                highest = i;
            }
            System.out.println("Answer 2 instances: " + i);

            p = Pattern.compile(answerStringsConcated[2].toLowerCase());
            m = p.matcher(inputSaved.toString().toLowerCase());
            i = 0;
            while (m.find()) {
                i++;
            }
            p = Pattern.compile(answerStringsConcated[2].toLowerCase().substring(0, answerStringsConcated[2].length()-1));
            m = p.matcher(inputSaved.toString().toLowerCase());
            while (m.find()) {
                i++;
            }
            if(i >= highest){
                whichHigh = 2;
                highest = i;
            }
            System.out.println("Answer 3 instances: " + i);

            String ansString = answerStringsConcated[whichHigh];
            System.out.println("The answer is probably \"" + ansString + "\"");

        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }

    }
}