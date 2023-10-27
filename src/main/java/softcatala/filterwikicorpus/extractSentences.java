package softcatala.filterwikicorpus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.languagetool.Language;
import org.languagetool.language.Catalan;

public class extractSentences {

  private static Language lang = new Catalan();
  
  private static int SENTENCES_PER_FILE = 1000000;

  public static void main(String[] args) throws Exception {
    String pathstr = args[0];
    String outPath = args[1]+"wiki";
    Path rootDir = Paths.get(pathstr);
    List<String> allfiles;
    allfiles = getFileNames(new ArrayList<String>(), rootDir);
    
    int inputfileCounter = 0;
    int fileCounter = 0;
    int lineCounter = 0;
    
    FileWriter fstream = new FileWriter(outPath + String.format("%04d" , fileCounter), true); 
    BufferedWriter out = new BufferedWriter(fstream);
    
    for (String inputFilename : allfiles) {
      inputfileCounter++;
      System.out.println(inputfileCounter + ":" +inputFilename);
      try (BufferedReader br = new BufferedReader(new FileReader(inputFilename))) {
        String line;
        while ((line = br.readLine()) != null) {
          // process the line.
          List<String> sentences = lang.getSentenceTokenizer().tokenize(line);
          for (String sentence: sentences) {
            out.append(sentence+"\n");
            lineCounter++;
            if (lineCounter==SENTENCES_PER_FILE) {
              lineCounter=0;
              out.close();
              fileCounter++;
              fstream = new FileWriter(String.format(outPath + "%04d" , fileCounter), true); 
              out = new BufferedWriter(fstream);
            }
            
          }
          
        }
      }
    }
    out.close();

  }

  private static List<String> getFileNames(List<String> fileNames, Path dir) {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
      for (Path path : stream) {
        if (path.toFile().isDirectory()) {
          getFileNames(fileNames, path);
        } else {
          fileNames.add(path.toAbsolutePath().toString());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return fileNames;
  }

}
