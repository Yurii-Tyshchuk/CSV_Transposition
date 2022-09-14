package test;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Resource {
    private final String pathToInputFile;
    private final String pathToOutputFile;
    private final String pathToFileWithUniqueID;
    private final List<String> listOfUniqueIDs = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss.SSS");
    private LocalDateTime firstDate;
    private LocalDateTime lastDate;
    private LocalDateTime currentLastDate;
    public Resource(String pathToInputFile, String pathToOutputFile, String pathToFileWithUniqueID) {
        this.pathToInputFile = pathToInputFile;
        this.pathToOutputFile = pathToOutputFile;
        this.pathToFileWithUniqueID = pathToFileWithUniqueID;
        init();
    }

    public void init() {
        try {
            File file = new File(pathToOutputFile);
            if (!file.exists()) {
                file.createNewFile();
            } else {
                new FileWriter(pathToOutputFile, false).close();
            }
            BufferedReader fileWithUniqueID = new BufferedReader(new FileReader(pathToFileWithUniqueID));
            String line;
            while ((line = fileWithUniqueID.readLine()) != null) {
                listOfUniqueIDs.add(line);
            }
            fileWithUniqueID.close();

            BufferedReader inputFile = new BufferedReader(new FileReader(pathToInputFile));

            inputFile.readLine();
            String firstDateTime = inputFile.readLine().split(";")[0];
            String lastDateTime = "";
            while ((line = inputFile.readLine()) != null) {
                lastDateTime = line;
            }
            lastDateTime = lastDateTime.split(";")[0];
            inputFile.close();

            firstDate = getDateTime(firstDateTime).truncatedTo(ChronoUnit.SECONDS);
            lastDate = getDateTime(lastDateTime).truncatedTo(ChronoUnit.SECONDS);
            currentLastDate = getDateTime(lastDateTime);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public LocalDateTime getDateTime(String firstDateTime) {
        return LocalDateTime.parse(firstDateTime.substring(0, firstDateTime.length() - 10), formatter);
    }

    public BufferedReader readCSV() {
        try {
            BufferedReader inputFile = new BufferedReader(new FileReader(pathToInputFile));
            inputFile.readLine();
            return inputFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeToCSV(String line) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(pathToOutputFile, true))) {
            bw.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        firstDate = firstDate.plusSeconds(1);
    }

    public LocalDateTime getFirstDate() {
        return firstDate;
    }

    public LocalDateTime getLastDate() {
        return lastDate;
    }

    public LocalDateTime getCurrentLastDate() {
        return currentLastDate;
    }

    public List<String> getListOfUniqueIDs() {
        return listOfUniqueIDs;
    }
}
