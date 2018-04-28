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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static WebCalls webCalls;

    public static void main(String[] args) throws IOException, AWTException {
        // System.setProperty("jna.library.path", "32".equals(System.getProperty("sun.arch.data.model")) ? "lib/win32-x86" : "lib/win32-x86-64");
        System.setProperty("wordnet.database.dir", "C:\\Program Files (x86)\\WordNet\\2.1\\dict\\");
        WebCalls webCalls = new WebCalls();
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("Enter to search:");

            in.nextLine();
            System.out.println("Searching...");
            search();
        }
    }

    private static void search() throws IOException, AWTException {
        BufferedImage question = new Robot().createScreenCapture(new Rectangle(10, 400, 910, 800));
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

            //Start count
            boolean not = false;
            if(questionString.contains("NOT")){
                not = true;
                questionString = questionString.replaceAll("NOT", "");
            }

            Search[] searchThreads = {
                    new QuestionGoogle(questionString, answerStringsConcated, questionEntities, synsets),
                    new EntityGoogle((ArrayList<Entity>) questionEntities.getResponse().getEntities(), (ArrayList<Word>) questionEntities.getResponse().getWords(), answerStringsConcated, synsets),
                    new EntityWiki((ArrayList<Entity>) questionEntities.getResponse().getEntities(), answerStringsConcated, synsets)};
            //new TopicWiki((ArrayList<Topic>) questionEntities.getResponse().getTopics(), answerStringsConcated, synsets, webCalls)};

            boolean haveAnswered = false;
            while (!haveAnswered) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                boolean done = true;

                for (Search search : searchThreads) {
                    if(!search.getFinished()) {
                        done = false;
                    }
                }

                if (done){//searchThreads[0].getFinished() && searchThreads[1].getFinished() && searchThreads[2].getFinished()) {//&& searchThreads[3].getFinished()) {

                    int[] ansNums = new int[3];
                    for (Search search : searchThreads) {
                        if (search.getPossibleAnswer().equals(answerStringsConcated[0])) {
                            ansNums[0]++;
                        } else if (search.getPossibleAnswer().equals(answerStringsConcated[1])) {
                            ansNums[1]++;
                        } else if (search.getPossibleAnswer().equals(answerStringsConcated[2])) {
                            ansNums[2]++;
                        }
                    }

                    if((ansNums[0] != ansNums[1] || ansNums[0] == 0 && ansNums[1] == 0)
                            && (ansNums[0] != ansNums[2] || ansNums[0] == 0 && ansNums[2] == 0)
                            && (ansNums[2] != ansNums[1] || ansNums[2] == 0 && ansNums[1] == 0)) {
                        String answerProbably = null;
                        int highestVote = 0;
                        HashMap<String, Integer> map = new HashMap<String, Integer>();
                        map.put(answerStringsConcated[0], ansNums[0]);
                        map.put(answerStringsConcated[1], ansNums[1]);
                        map.put(answerStringsConcated[2], ansNums[2]);
                        if(!not) {
                            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                                if (entry.getValue() > highestVote) {
                                    answerProbably = entry.getKey();
                                    highestVote = entry.getValue();
                                }
                            }
                        }
                        else{
                            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                                if (entry.getValue() <= highestVote) {
                                    answerProbably = entry.getKey();
                                    highestVote = entry.getValue();
                                }
                            }
                        }
                        System.out.println("By common consensus the highest answer might be \"" + answerProbably + "\"");
                        haveAnswered = true;
                    }
                    else {
                        if(ansNums[0] == ansNums[1]){
                            System.out.println("By common consensus the highest answer might be \"" + answerStringsConcated[0] + "\"" + "(Question) or \"" + answerStringsConcated[1] + "\"(Entity Google)");
                            haveAnswered = true;
                        }
                        else if(ansNums[0] == ansNums[2]){
                            System.out.println("By common consensus the highest answer might be \"" + answerStringsConcated[0] + "\"" + "(Question) or \"" + answerStringsConcated[2] + "\"(Entity Wiki)");
                            haveAnswered = true;
                        }
                        else{
                            System.out.println("By common consensus the highest answer might be \"" + answerStringsConcated[1] + "\"" + "(Entity Google) or \"" + answerStringsConcated[2] + "\"(Entity Wiki)");
                            haveAnswered = true;
                        }
                    }
                }
            }
        } catch (TesseractException e1) {
            e1.printStackTrace();
        } catch (AnalysisException e1) {
            e1.printStackTrace();
        }
    }
}