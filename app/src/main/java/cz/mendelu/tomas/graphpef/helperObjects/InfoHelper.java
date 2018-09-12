package cz.mendelu.tomas.graphpef.helperObjects;

import java.util.ArrayList;

/**
 * Created by tomas on 02.09.2018.
 */

public class InfoHelper {
    private String title;
    ArrayList<String> texts;

    public InfoHelper() {
        title = "";
        texts = new ArrayList<>();

        populateTexts();
    }

    //TODO implement database with texts
    private void populateTexts(){
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getTexts() {
        return texts;
    }
}
