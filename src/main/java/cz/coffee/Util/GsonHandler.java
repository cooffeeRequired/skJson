package cz.coffee.Util;


import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class GsonHandler extends Util {

    final private File file;
    private boolean exist;

    public GsonHandler(File file) {
        super();
        this.file = file;
    }

    private boolean makeFile(String str) {

        if (this.file.exists()) {
            if(this.file.length() != 0) {
                return false;
            }
        }

        JsonElement je = parsedString(str);
        // Trying to make a new file
        FileWriter f = null;
        try {
            f = new FileWriter(this.file);
            prettyGson().toJson(je, f);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert f != null;
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean removeFile() {

        if (this.file.exists()) {
            boolean deleted = this.file.delete();
            if ( deleted ){
                return this.file.isFile();
            }
        }
        return false;
    }


    private boolean writeFile( String str) {
        if (this.file.exists()) {
            JsonElement je = parsedString(str);
            // Trying to get contents of File and parse the new Data to finally object

            JsonReader r = null;
            FileWriter f = null;
            try {
                // Read
                r = new JsonReader(new FileReader(this.file));
                JsonElement objectFromFile = prettyGson().fromJson(r, JsonElement.class);
                System.out.println(prettyGson().toJson(objectFromFile));


                // Append a new Object
                f = new FileWriter(this.file);
                System.out.println(prettyGson().toJson(je));


                //prettyGson().toJson(je, f);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    assert f != null;
                    // f.close();
                    r.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }




    public void makeJsonFile(String... String) {
        String jsonString = String.length > 0 ? String[0] : "{}";
        makeFile(jsonString);
    }
    public void removeJsonFile() {
        removeFile();
    }

    public void writeJsonFile(String... String){
        String jsonString = String.length > 0 ? String[0] : "{}";
        writeFile(jsonString);
    }


}
