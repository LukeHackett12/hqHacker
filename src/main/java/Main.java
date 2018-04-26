import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.textrazor.AnalysisException;
import com.textrazor.TextRazor;
import com.textrazor.annotations.AnalyzedText;
import com.textrazor.annotations.Entity;
import com.textrazor.annotations.Word;
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
import java.util.Map;
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
        BufferedImage question = new Robot().createScreenCapture(new Rectangle(10, 300, 910, 900));
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
            client.addExtractor("nounPhrases");
            client.addExtractor("entailments");
            client.addExtractor("properties");

            AnalyzedText questionEntities = client.analyze(questionString);

            if (questionEntities.getResponse().getEntities() != null) {
                for (Entity entity : questionEntities.getResponse().getEntities()) {
                    System.out.println("Entity: " + entity.getEntityId());
                    System.out.println("Relevance: " + entity.getRelevanceScore());
                    System.out.println("Confidence: " + entity.getConfidenceScore());
                }
            }

            //Construct URL
            URL url = constructURLNormal(questionString, answerStringsConcated);

            //Call URL
            String webPage = urlCall(url);

            //Start count
            boolean ansFound = false;
            int whichHigh = highestMatch(webPage, answerStringsConcated, synsets);
            if (whichHigh != -1) {
                String ansString = answerStringsConcated[whichHigh];
                System.out.println("The answer is probably \"" + ansString + "\"");
            } else {
                if ((url = constructURLEntity((ArrayList<Entity>) questionEntities.getResponse().getEntities(), (ArrayList<Word>) questionEntities.getResponse().getWords())) != null) {
                    webPage = urlCall(url);
                    whichHigh = highestMatch(webPage, answerStringsConcated, synsets);
                    if (whichHigh != -1) {
                        String ansString = answerStringsConcated[whichHigh];
                        System.out.println("The answer is probably \"" + ansString + "\"");
                    }
                } else if (questionEntities.getResponse().getEntities() != null) {
                    if (questionEntities.getResponse().getEntities().get(0).getRelevanceScore() > 0.0f) {
                        for (int i = 0; i < questionEntities.getResponse().getEntities().size(); i++) {
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
                    }
                    if (!ansFound) {
                        System.out.println("Can't find an answer");
                    }
                } else if (questionEntities.getResponse().getTopics() != null) {
                    for (int i = 0; i < questionEntities.getResponse().getTopics().size(); i++) {
                        webPage = urlCall(new URL(questionEntities.getResponse().getTopics().get(i).getWikiLink()));
                        whichHigh = highestMatch(webPage, answerStringsConcated, synsets);
                        if (whichHigh != -1) {
                            String ansString = answerStringsConcated[whichHigh];
                            System.out.println("The answer is probably \"" + ansString + "\"");
                            ansFound = true;
                            break;
                        }
                    }
                    if (!ansFound) {
                        System.out.println("Can't find an answer");
                    }
                } else {
                    System.out.println("Can't find an answer");
                }
            }

        } catch (TesseractException e1) {
            e1.printStackTrace();
        } catch (AnalysisException e1) {
            e1.printStackTrace();
        }
    }

    private static int highestMatch(String inputSaved, String[] answerStringsConcated, Synset[][] synsets) {
        int tempComparator = 0;
        int highest = 0;
        int whichHigh = 0;

        for (int i = 0; i < 3; i++) {
            tempComparator = numMatches(inputSaved, answerStringsConcated[i]) + numMatches(inputSaved, answerStringsConcated[i].toLowerCase().substring(0, answerStringsConcated[i].length() - 1));
            //Synonyms
            for (Synset synset : synsets[i]) {
                for (String s : synset.getWordForms()) {
                    tempComparator += numMatches(inputSaved, s);
                }
            }
            if (tempComparator >= highest) {
                whichHigh = i;
                highest = tempComparator;
            }
        }

        if (highest == 0) {
            for (int i = 0; i < 3; i++) {
                if (answerStringsConcated[i].split(" ").length > 1) {
                    String[] split = answerStringsConcated[i].split(" ");
                    for (String aSplit : split) {
                        tempComparator += numMatches(inputSaved, aSplit);
                    }
                }
                if (tempComparator >= highest) {
                    whichHigh = i;
                    highest = tempComparator;
                }
            }
            if(highest != 0) return whichHigh;
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

    private static URL constructURLNormal(String questionString, String[] answerStringsConcated) throws MalformedURLException {
        String url = "https://www.googleapis.com/customsearch/v1?q=" + questionString.replaceAll(" ", "+").replaceAll("\"", "") +
                "&as_oq=" + "%22" + answerStringsConcated[0].replaceAll(" ", "+") + "%22" + "+" + "%22" + answerStringsConcated[1].replaceAll(" ", "+") + "%22" + "+" + "%22" + answerStringsConcated[2].replaceAll(" ", "+") + "%22" +
                //"&orTerms=" + answerStringsConcated[0].replaceAll(" ", "+") + "," + answerStringsConcated[1].replaceAll(" ", "+") + "," + answerStringsConcated[2].replaceAll(" ", "+") +
                "&cx=016409237003735062340%3Anfsrbopx80k" +
                "&key=AIzaSyBMsx3u8GCtyYT1akAv0zRNiQuxuxQJt1I";

        return new URL(url.replaceAll(" ", "%20"));
    }

    private static URL constructURLEntity(ArrayList<Entity> entities, ArrayList<Word> words) throws MalformedURLException {
        StringBuilder url = new StringBuilder("https://www.googleapis.com/customsearch/v1?q=");

        if (entities != null) {
            if (entities.get(0).getRelevanceScore() > 0.0f) {
                for (Entity e : entities) {
                    url.append(e.getEntityId()).append(" ");
                }
                url = new StringBuilder(url.substring(0, url.length() - 1));
                if(words != null){
                    for(Word w : words){
                        if(w.getPartOfSpeech().equals("JJS")){
                            url.append(w).append(" ");
                        }
                    }
                }

                System.out.println(url.toString().replaceAll(" ", "%20"));
                return new URL(url.toString().replaceAll(" ", "%20"));
            }
        }
        return null;
    }

    private static String urlCall(URL url) throws IOException {
        if (url.toString().contains("google")) {
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