package softcatala.filterwikicorpus;

import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.Catalan;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class filterwikicorpus {
  public filterwikicorpus() {
  }

  private static Language lang = new Catalan();
  private static int SENTENCE_CHAR_MAX_SIZE = 115;
  private static int SENTENCE_CHAR_MIN_SIZE = 12;
  private static JLanguageTool langTool = new JLanguageTool(new Catalan());
  private static final Pattern VALID_SENTENCE = Pattern.compile("^[«»]?[A-ZÀÈÉÍÒÓÚ].+[\\.!?]$");
  private static final Pattern INVALID_SENTENCE = Pattern
      .compile(".*([0-9]|[A-Z]\\.|art\\.|núm\\.|[A-ZÀÈÉÍÒÓÚ][A-ZÀÈÉÍÒÓÚ]|[\\(\\)\\[\\]śźń:]).*");

  //
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("Ús: java -jar filtercorpus.jar json_files_path ouputfile");
      System.exit(1);
    }
    String pathstr = args[0];
    String outputFilename = args[1];

    PrintWriter out = new PrintWriter(outputFilename);

    JSONParser parser = new JSONParser();
    Path rootDir = Paths.get(pathstr);
    List<String> allfiles;
    allfiles = getFileNames(new ArrayList<String>(), rootDir);

    List<String> allsentences = new ArrayList<String>();

    langTool.disableRules(Arrays.asList("EXIGEIX_VERBS_CENTRAL", "EXIGEIX_ACCENTUACIO_GENERAL", "EXIGEIX_POSSESSIUS_V",
        "EVITA_PRONOMS_VALENCIANS", "EVITA_DEMOSTRATIUS_EIXE", "VOCABULARI_VALENCIA", "EXIGEIX_US"));

    for (String inputFilename : allfiles) {
      try (BufferedReader br = new BufferedReader(new FileReader(inputFilename))) {
        String line;
        while ((line = br.readLine()) != null) {
          // process the line.
          Object obj = parser.parse(line);
          JSONObject jsonObject = (JSONObject) obj;
          String text = (String) jsonObject.get("text");

          List<String> sentences = lang.getSentenceTokenizer().tokenize(text);
          int numSentences = 0;
          for (String s : sentences) {
            String str = s.trim();
            if (str.startsWith("--"))
              str = str.substring(2);
            if (str.startsWith("-") || str.startsWith("—") 
                || str.startsWith("–") || str.startsWith("»")) {
              str = str.substring(1);
            }
            str = s.trim();
            if (allsentences.contains(str)) {
              continue;
            }
            if ((str.length() > SENTENCE_CHAR_MIN_SIZE) && (str.length() < SENTENCE_CHAR_MAX_SIZE)
                && (!str.endsWith("...")) && (VALID_SENTENCE.matcher(str).matches())
                && (!INVALID_SENTENCE.matcher(str).matches()) && (langTool.check(str).isEmpty())) {
              out.println(str);
              allsentences.add(str);
              numSentences++;
            }
            if (numSentences == 3) {
              break;
            }
          }
          // out.println("** " + (String) jsonObject.get("title")+": "+numSentences);
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
