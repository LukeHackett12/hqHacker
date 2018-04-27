import edu.smu.tspell.wordnet.Synset;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class QuestionGoogle extends Thread implements Search{
    private String question;
    private String[] answerStringsConcated;
    private Synset[][] synsets;

    private String possibleAnswer;
    private boolean finished;

    QuestionGoogle(String question, String[] answerStringsConcated, Synset[][] synsets, WebCalls webCalls){
        this.question = question;
        this.answerStringsConcated = answerStringsConcated;
        this.synsets = synsets;
        possibleAnswer = "";
        finished = false;
        start();
    }

    public void run(){
        //Question google start
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
            System.out.println("This is the plain old way");
            System.out.println("The answer is probably \"" + possibleAnswer + "\"");
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
