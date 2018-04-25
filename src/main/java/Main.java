import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.textrazor.AnalysisException;
import com.textrazor.TextRazor;
import com.textrazor.annotations.AnalyzedText;
import com.textrazor.annotations.Entity;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static WebClient webClient;

    public static void main(String[] args) throws IOException, AWTException {
        // System.setProperty("jna.library.path", "32".equals(System.getProperty("sun.arch.data.model")) ? "lib/win32-x86" : "lib/win32-x86-64");
        System.setProperty("wordnet.database.dir", "C:\\Program Files (x86)\\WordNet\\2.1\\dict\\");
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("Spam to search");
            if (in.hasNext()) {
                System.out.println("Searching...");
                search();
                in.next();
            }
        }
    }

    private static void search() throws IOException, AWTException {
        BufferedImage question = new Robot().createScreenCapture(new Rectangle(10, 300, 700, 975));
        ImageIO.write(question, "png", new File("question.png"));

        File questionImage = new File("question.png");
        ITesseract instance = new Tesseract();  // JNA Interface Mapping

        try {
            String fullString = instance.doOCR(questionImage).replaceAll("ﬁ", "fi");
            String questionString = (fullString.split("\\?")[0] + "?").replaceAll("\n", " ");
            System.out.println("Question: " + questionString);
            String[] answerStrings = fullString.split("\\?")[1].split("\n");

            String[] answerStringsConcated = new String[3];

            int j = 0;
            for (String answerString : answerStrings) {
                if (!answerString.equals("")) {
                    if (j == 3) break;
                    answerStringsConcated[j] = answerString;
                    j++;
                }
            }

            System.out.println("Possible Answers: ");
            for (String s : answerStringsConcated) {
                System.out.println(s);
            }

            //Get Synonyms of answers:
            WordNetDatabase database = WordNetDatabase.getFileInstance();
            Synset[][] synsets = {database.getSynsets(answerStringsConcated[0]), database.getSynsets(answerStringsConcated[1]), database.getSynsets(answerStringsConcated[2])};

            //Do some text analysis
            TextRazor client = new TextRazor("6d663ab2029c17f58afbba07ddc3c1231bb7483f22145cef788719d4");

            client.addExtractor("topics");
            client.addExtractor("entities");

            AnalyzedText questionEntities = client.analyze(questionString);
            if(questionEntities.getResponse().getEntities() != null) {
                for (Entity entity : questionEntities.getResponse().getEntities()) {
                    System.out.println("Entity: " + entity.getEntityId());
                    System.out.println("Relevance: " + entity.getRelevanceScore());
                    System.out.println("Confidence: " + entity.getConfidenceScore());
                }
            }

            //Construct URL
            URL url = constructURL(questionString, (ArrayList<Entity>) questionEntities.getResponse().getEntities(), answerStringsConcated);

            //Call URL
            String webPage = urlCall(url);

            //Start count
            boolean ansFound = false;
            int whichHigh = highestMatch(webPage, answerStringsConcated, synsets);
            if (whichHigh != -1) {
                String ansString = answerStringsConcated[whichHigh];
                System.out.println("The answer is probably \"" + ansString + "\"");
            } else {
                if (questionEntities.getResponse().getEntities() != null) {
                    for(int i = 0; i < questionEntities.getResponse().getEntities().size(); i++){
                        webPage = urlCall(new URL(questionEntities.getResponse().getEntities().get(i).getWikiLink()));
                        System.out.println(webPage);
                        whichHigh = highestMatch(webPage, answerStringsConcated, synsets);
                        if (whichHigh != -1) {
                            String ansString = answerStringsConcated[whichHigh];
                            System.out.println("The answer is probably \"" + ansString + "\"");
                            ansFound = true;
                            break;
                        }
                    }
                    if(!ansFound) {
                        System.out.println("Can't find an answer");
                    }
                } else if(questionEntities.getResponse().getTopics() != null){
                    for(int i = 0; i < questionEntities.getResponse().getTopics().size(); i++){
                        webPage = urlCall(new URL(questionEntities.getResponse().getTopics().get(i).getWikiLink()));
                        System.out.println(webPage);
                        whichHigh = highestMatch(webPage, answerStringsConcated, synsets);
                        if (whichHigh != -1) {
                            String ansString = answerStringsConcated[whichHigh];
                            System.out.println("The answer is probably \"" + ansString + "\"");
                            ansFound = true;
                            break;
                        }
                    }
                    if(!ansFound) {
                        System.out.println("Can't find an answer");
                    }
                } else {
                    System.out.println("Can't find an answer");
                }
            }

        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        } catch (AnalysisException e) {
            e.printStackTrace();
        }
    }

    private static int highestMatch(String inputSaved, String[] answerStringsConcated, Synset[][] synsets) {
        int tempComparator = 0;
        int highest = 0;
        int whichHigh = 0;

        tempComparator = numMatches(inputSaved, answerStringsConcated[0]) + numMatches(inputSaved, answerStringsConcated[0].toLowerCase().substring(0, answerStringsConcated[0].length() - 1));
        if (tempComparator >= highest) {
            whichHigh = 0;
            highest = tempComparator;
        }
        System.out.println("Answer 1 instances: " + tempComparator);

        tempComparator = numMatches(inputSaved, answerStringsConcated[1]) + numMatches(inputSaved, answerStringsConcated[1].toLowerCase().substring(0, answerStringsConcated[1].length() - 1));
        if (tempComparator >= highest) {
            whichHigh = 1;
            highest = tempComparator;
        }
        System.out.println("Answer 2 instances: " + tempComparator);

        tempComparator = numMatches(inputSaved, answerStringsConcated[2]) + numMatches(inputSaved, answerStringsConcated[2].toLowerCase().substring(0, answerStringsConcated[2].length() - 1));
        if (tempComparator >= highest) {
            whichHigh = 2;
        }
        System.out.println("Answer 3 instances: " + tempComparator);

        if (highest == 0) {
            return -1;
        }
        return whichHigh;
    }

    private static int numMatches(String inputSaved, String answerToCheck) {
        Pattern p = Pattern.compile(answerToCheck.toLowerCase());
        Matcher m = p.matcher(inputSaved.toLowerCase());
        int i = 0;
        while (m.find()) {
            i++;
        }
        return i;
    }

    private static URL constructURL(String questionString, ArrayList<Entity> entities, String[] answerStringsConcated) throws MalformedURLException {
        String url = "https://www.googleapis.com/customsearch/v1?q=";
        if(entities != null) {
            for (Entity e : entities) {
                url += e.getEntityId() + " ";
            }
            url = url.substring(0, url.length() - 1);
        } else {
            url += questionString.replaceAll(" ", "+").replaceAll("\"", "").split("which")[0] + "?";
        }
        url += "&cx=016409237003735062340%3Anfsrbopx80k" +
                //"&orTerms=" + answerStringsConcated[0].replaceAll(" ", "+") + "," + answerStringsConcated[1].replaceAll(" ", "+") + "," + answerStringsConcated[2].replaceAll(" ", "+") +
                "&key=AIzaSyBMsx3u8GCtyYT1akAv0zRNiQuxuxQJt1I";

        return new URL(url.replaceAll(" ", "%20"));
    }

    private static String urlCall(URL url) throws IOException {
        if(url.toString().contains("google")) {
            URLConnection urlConnect = url.openConnection();
            urlConnect.addRequestProperty("User-Agent",
                    "Mozilla");
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnect.getInputStream()));

            //Retrieve info
            String inputLine;
            StringBuilder inputSaved = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                inputSaved.append(inputLine);
            in.close();
            return inputSaved.toString();
        } else {
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(false);

            HtmlPage page = webClient.getPage(url);
            return page.asText();
        }
    }
}