import com.textrazor.annotations.Entity;
import edu.smu.tspell.wordnet.Synset;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class EntityWiki extends Thread implements Search{
    private ArrayList<Entity> entities;
    private String[] answerStringsConcated;
    private Synset[][] synsets;

    private String possibleAnswer;
    private boolean finished;

    EntityWiki(ArrayList<Entity> entities, String[] answerStringsConcated, Synset[][] synsets, WebCalls webCalls) {
        this.entities = entities;
        this.answerStringsConcated = answerStringsConcated;
        this.synsets = synsets;
        finished = false;
        possibleAnswer = "";
        start();
    }

    public void run() {
        if(entities != null) {
            if (entities.get(0).getRelevanceScore() > 0.0f) {
                for (Entity entity : entities) {
                    String webPage = null;
                    try {
                        webPage = WebCalls.urlCall(new URL(entity.getWikiLink()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int whichHigh = Counter.highestMatch(webPage, answerStringsConcated, synsets);
                    if (whichHigh != -1) {
                        possibleAnswer = answerStringsConcated[whichHigh];
                        System.out.println("This is the entity wiki way");
                        System.out.println("The answer is probably \"" + possibleAnswer + "\"");
                        break;
                    }
                }
            }
        }

        finished = true;
    }

    public String getPossibleAnswer(){
        return possibleAnswer;
    }

    public boolean getFinished() {
        return finished;
    }
}
