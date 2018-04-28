import com.textrazor.annotations.Topic;
import edu.smu.tspell.wordnet.Synset;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class TopicWiki extends Thread implements Search{
    private ArrayList<Topic> topics;
    //private ArrayList<Word> words;
    private String[] answerStringsConcated;
    private Synset[][] synsets;

    private String possibleAnswer;
    private boolean finished;

    TopicWiki(ArrayList<Topic> topics, /* ArrayList<Word> words,*/ String[] answerStringsConcated, Synset[][] synsets, WebCalls webCalls) {
        this.topics = topics;
        //this.words = words;
        this.answerStringsConcated = answerStringsConcated;
        this.synsets = synsets;
        possibleAnswer = "";
        finished = false;
        start();
    }

    public void run() {
        if(topics != null) {
            for (Topic topic : topics) {
                String webPage = null;
                try {
                    webPage = WebCalls.urlCall(new URL(topic.getWikiLink()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(webPage);
                int whichHigh = Counter.highestMatch(webPage, answerStringsConcated, synsets);
                if (whichHigh != -1) {
                    possibleAnswer = answerStringsConcated[whichHigh];
                    break;
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
