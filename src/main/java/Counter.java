import edu.smu.tspell.wordnet.Synset;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Counter {
    public static int highestMatch(String inputSaved, String[] answerStringsConcated, Synset[][] synsets) {
        int tempComparator = 0;
        int highest = 0;
        int whichHigh = 0;

        for (int i = 0; i < 3; i++) {
            tempComparator = numMatches(inputSaved, answerStringsConcated[i]);// + numMatches(inputSaved, answerStringsConcated[i].toLowerCase().substring(0, answerStringsConcated[i].length() - 1));
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

        if (highest == 0)

        {
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
}
