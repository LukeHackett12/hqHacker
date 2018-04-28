import com.textrazor.annotations.Entity;
import com.textrazor.annotations.Word;
import edu.smu.tspell.wordnet.Synset;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class EntityGoogle extends Thread implements Search {
    private ArrayList<Entity> entities;
    private ArrayList<Word> words;
    private String[] answerStringsConcated;
    private Synset[][] synsets;

    private String possibleAnswer;
    private boolean finished;

    EntityGoogle(ArrayList<Entity> entities, ArrayList<Word> words, String[] answerStringsConcated, Synset[][] synsets) {
        this.entities = entities;
        this.words = words;
        this.answerStringsConcated = answerStringsConcated;
        this.synsets = synsets;
        possibleAnswer = "";
        finished = false;
        start();
    }

    public void run() {
        if (entities != null) {
            URL url = null;
            try {
                url = WebCalls.constructURLEntity(entities, words);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            String webPage = null;
            try {
                webPage = WebCalls.urlCall(url);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            try {
                int whichHigh = Counter.highestMatch(webPage, answerStringsConcated, synsets);
                if (whichHigh != -1) {
                    possibleAnswer = answerStringsConcated[whichHigh];
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        finished = true;
    }

    public String getPossibleAnswer() {
        return possibleAnswer;
    }

    public boolean getFinished() {
        return finished;
    }
}
