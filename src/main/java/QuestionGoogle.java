import com.textrazor.annotations.AnalyzedText;
import edu.smu.tspell.wordnet.Synset;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class QuestionGoogle extends Thread implements Search{
    private String question;
    private String[] answerStringsConcated;
    private AnalyzedText analyzedText;
    private Synset[][] synsets;

    private String possibleAnswer;
    private boolean finished;

    QuestionGoogle(String question, String[] answerStringsConcated, AnalyzedText analyzedText, Synset[][] synsets){
        this.question = question;
        this.answerStringsConcated = answerStringsConcated;
        this.analyzedText = analyzedText;
        this.synsets = synsets;
        possibleAnswer = "";
        finished = false;
        start();
    }

    public void run(){
        //Question google start
        if(!question.toLowerCase().contains("which")) {
            URL url = null;
            try {
                url = WebCalls.constructURLNormal(question, answerStringsConcated);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String webPage = null;
            try {
                webPage = WebCalls.urlCall(url);
            } catch (IOException e) {
                System.out.println("hops");
            }
            int whichHigh = Counter.highestMatch(webPage, answerStringsConcated, synsets);
            if (whichHigh != -1) {
                possibleAnswer = answerStringsConcated[whichHigh];
            }
        }
        else {
            URL[] url = null;
            try {
                url = WebCalls.constructURLWhich(answerStringsConcated);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            ArrayList<String> webPage = new ArrayList<String>();
            try {
                for(URL u : url) {
                    String wp = WebCalls.urlCall(u);
                    webPage.add(wp);
                }
            } catch (IOException e) {
                System.out.println("hops");
            }
            int whichHigh = Counter.highestMatchWhich(webPage, analyzedText, synsets);
            if (whichHigh != -1) {
                possibleAnswer = answerStringsConcated[whichHigh];
            }
        }
        //Question google end
        finished = true;
    }

    public String getPossibleAnswer(){
        return possibleAnswer;
    }

    public boolean getFinished(){

        return finished;
    }
}
