package swipe.util;

import swipe.data.Constants;
import swipe.data.Timestamp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class LogManager {

    public static void appendLog(String contents){
        Constants.logContents += contents + '\n';
        writeFile();
    }

    public static void appendLogWithTimeStamp(String string){
        String timeStamp = Timestamp.getCurrentTime();
        Constants.logContents += "[" + timeStamp + "] " + string + '\n';
        writeFile();
    }

    public static void readFileIntoContents() {
        try {
            Constants.logContents = new String(Files.readAllBytes(Paths.get(Constants.logFile.getPath())));
            if(Constants.logContents == null){
                Constants.logContents = "Log File Not Found ---- Data will not persist after closing";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(){
        try(PrintWriter out = new PrintWriter(Constants.logFile.toPath().toString())){
            out.println(Constants.logContents);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
