import com.textrazor.annotations.AnalyzedText;
import com.textrazor.annotations.Word;
import edu.smu.tspell.wordnet.Synset;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Counter {
    public static int highestMatch(String inputSaved, String[] answerStringsConcated, Synset[][] synsets) {
        int tempComparator = 0;
        int highest = 0;
        int whichHigh = 0;

        for (int i = 0; i < 3; i++) {
            tempComparator = numMatches(inputSaved, answerStringsConcated[i]);
            if(answerStringsConcated[i].contains("/")){
                tempComparator += numMatches(inputSaved, answerStringsConcated[i].toLowerCase().split(" / ")[0]);//+ numMatches(inputSaved, answerStringsConcated[i].toLowerCase().split("/")[0]);
            }
            //Synonyms
            try {
                if (synsets != null) {
                    if (synsets[i] != null) {
                        for (Synset s : synsets[i]) {
                            tempComparator += numMatches(inputSaved, s.getWordForms()[0]);
                        }

                        if (tempComparator >= highest) {
                            whichHigh = i;
                            highest = tempComparator;
                        }
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
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
            if (highest != 0) return whichHigh;
            return -1;
        }
        return whichHigh;
    }

    public static int highestMatchWhich(ArrayList<String> inputPages, AnalyzedText question, Synset[][] synsets){
        int whichHigh = 0;
        int highest = 0;

        int i = 0;
        //TODO take nouns
        ArrayList<String> nounList = new ArrayList<String>();
        for(Word w : question.getResponse().getWords()){
            if(w.getPartOfSpeech().equals("NN")) nounList.add(w.getLemma());
        }

        String[] nouns = new String[nounList.size()];
        int j = 0;
        for(String s : nounList){
            nouns[j] = s.toLowerCase();
            j++;
        }

        for(String savedPage : inputPages){
            int tempComparator = 0;
            tempComparator += numMatches(savedPage.toLowerCase(), nouns);

            if (tempComparator >= highest) {
                whichHigh = i;
                highest = tempComparator;
            }
            i++;
        }

        return whichHigh;
    }

    private static int numMatches(String inputSaved, String answerToCheck) {
        try {
            Pattern p = Pattern.compile(answerToCheck.toLowerCase());
            Matcher m = p.matcher(inputSaved.toLowerCase());
            int i = 0;
            while (m.find()) {
                i++;
            }
            return i;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int numMatches(String inputSaved, String[] answerToCheck) {
        try {
            int add = 0;
            for(String s : answerToCheck) {
                Pattern p = Pattern.compile(s.toLowerCase());
                Matcher m = p.matcher(inputSaved.toLowerCase());
                int i = 0;
                while (m.find()) {
                    i++;
                }
                add += i;
            }
            return add;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
