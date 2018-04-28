import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.textrazor.annotations.Entity;
import com.textrazor.annotations.Word;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class WebCalls {
    private static WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);

    public static URL constructURLNormal(String questionString, String[] answerStringsConcated) throws MalformedURLException {
        String url = "https://www.googleapis.com/customsearch/v1?q=" + questionString.replaceAll(" ", "+").replaceAll("\"", "") +
                "&as_oq=" + "%22" + answerStringsConcated[0].replaceAll(" ", "+") + "%22" + "+" + "%22" + answerStringsConcated[1].replaceAll(" ", "+") + "%22" + "+" + "%22" + answerStringsConcated[2].replaceAll(" ", "+") + "%22";

        /*
         if(questionString.toLowerCase().contains("which")){
            url += "&orTerms=" + answerStringsConcated[0].replaceAll(" ", "+") + "," + answerStringsConcated[1].replaceAll(" ", "+") + "," + answerStringsConcated[2].replaceAll(" ", "+");
        }
        //*/

        url += "&cx=016409237003735062340%3Anfsrbopx80k" +
                "&key=AIzaSyBMsx3u8GCtyYT1akAv0zRNiQuxuxQJt1I";

        return new URL(url.replaceAll(" ", "%20"));
    }

    public static URL[] constructURLWhich(String whichAnswers[]) throws MalformedURLException {
        URL[] whichURL = new URL[3];

        int i = 0;
        for(String s : whichAnswers){
            URL url = new URL("https://www.googleapis.com/customsearch/v1?q=" + s.replaceAll(" ", "+").replaceAll("\"", "") +
                    "&cx=016409237003735062340%3Anfsrbopx80k" +
                    "&key=AIzaSyBMsx3u8GCtyYT1akAv0zRNiQuxuxQJt1I");

            whichURL[i] = url;
            i++;
        }

        return whichURL;
    }

    public static URL constructURLEntity(ArrayList<Entity> entities, ArrayList<Word> words) throws MalformedURLException {
        StringBuilder url = new StringBuilder("https://www.googleapis.com/customsearch/v1?q=");

        if (entities != null) {
            if (entities.get(0).getRelevanceScore() > 0.0f) {
                for (Entity e : entities) {
                    url.append(e.getEntityId()).append(" ");
                }
                url = new StringBuilder(url.substring(0, url.length() - 1));
                if (words != null) {
                    for (Word w : words) {
                        if (w.getPartOfSpeech().equals("JJS") || w.getPartOfSpeech().equals("NN")) {
                            url.append(w.getLemma()).append(" ");
                        }
                    }
                }
                url.append("&cx=016409237003735062340%3Anfsrbopx80k&key=AIzaSyBMsx3u8GCtyYT1akAv0zRNiQuxuxQJt1I");

                //System.out.println(url.toString().replaceAll(" ", "%20"));
                return new URL(url.toString().replaceAll(" ", "%20"));
            }
        }
        return null;
    }

    public static String urlCall(URL url) throws IOException {
        if(url != null) {
            if (url.toString().contains("google")) {
                try {
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                webClient.getOptions().setCssEnabled(false);
                webClient.getOptions().setJavaScriptEnabled(false);

                try {
                    HtmlPage page = webClient.getPage(url);
                    return page.asText();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
