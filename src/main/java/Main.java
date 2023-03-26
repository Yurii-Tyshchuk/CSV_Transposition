import me.tongfei.progressbar.ProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String pathToInputFile = args[0];
        String pathToOutputFile = args[1];
        String pathToFileWithUniqueID = args[2];
        Resource resource = new Resource(pathToInputFile, pathToOutputFile, pathToFileWithUniqueID);

        List<String> listOfUniqueIDs = resource.getListOfUniqueIDs();
        OneRow.setListOfUniqueIDs(listOfUniqueIDs);

        Map<LocalDateTime, OneRow> listOfRows = resource.getListOfRows();
        HashSet<LocalDateTime> buffer = new HashSet<>();

        try (BufferedReader bufferedReader = resource.readCSV();
             BufferedReader bufferedReader2 = resource.readCSV();
             OutputStreamWriter outputStreamWriter = resource.getOutputStreamWriter();
             ProgressBar pb = new ProgressBar("Number of dates", bufferedReader2.lines().count())) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] columns = line.split(";");
                LocalDateTime currentTime = resource.getDateTime(columns[0]);

                LocalDateTime roundTime = currentTime.truncatedTo(ChronoUnit.SECONDS);
                LocalDateTime roundTimePlusSecond = roundTime.plus(Duration.ofSeconds(1));
                LocalDateTime roundTimeMinusSecond = roundTime.minus(Duration.ofSeconds(1));

                String value = columns[1];
                String tagName = columns[2];

                if (listOfUniqueIDs.contains(tagName)) {
                    OneRow row = listOfRows.get(roundTime);
                    row.addValueBySensorId(tagName, Double.parseDouble(value));
                    if (roundTime.plus(Duration.ofMillis(500)).isAfter(currentTime)) {
                        if (listOfRows.containsKey(roundTimeMinusSecond))
                            listOfRows.get(roundTimeMinusSecond).addValueBySensorId(tagName, Double.parseDouble(value));
                    }
                    if (roundTime.plus(Duration.ofMillis(500)).isBefore(currentTime)) {
                        if (listOfRows.containsKey(roundTimePlusSecond))
                            listOfRows.get(roundTimePlusSecond).addValueBySensorId(tagName, Double.parseDouble(value));
                    }
                }
                pb.step();

                if (buffer.size() == 500) {
                    List<LocalDateTime> sortedDate = new ArrayList<>(buffer);
                    Collections.sort(sortedDate);
                    for (LocalDateTime time : sortedDate.stream().limit(450).toList()) {
                        line = listOfRows.get(time).getLine();
                        outputStreamWriter.write(line);
                        listOfRows.remove(time);
                        buffer.remove(time);
                    }

                }

                buffer.add(roundTime);
            }

            List<LocalDateTime> sortedDate = new ArrayList<>(buffer);
            Collections.sort(sortedDate);
            for (LocalDateTime time : sortedDate) {
                line = listOfRows.get(time).getLine();
                outputStreamWriter.write(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
