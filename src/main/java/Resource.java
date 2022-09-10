import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Resource {
    private final String pathToInputFile;
    private final String pathToOutputFile;
    private final String pathToFileWithUniqueID;
    private final List<String> listOfUniqueIDs = new ArrayList<>();
    private List<LocalDateTime> intervalOfDateTime;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss.SSS");
    private LocalDateTime firstDate;

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
            String lastDateTime = null;

            while ((line = inputFile.readLine()) != null) {
                lastDateTime = line.split(";")[0];
            }
            inputFile.close();
            if (lastDateTime == null)
                throw new RuntimeException("Empty last date");

            firstDate = getDateTime(firstDateTime);
            LocalDateTime lastDate = getDateTime(lastDateTime);
            long between = ChronoUnit.SECONDS.between(firstDate, lastDate);
            intervalOfDateTime = IntStream
                    .iterate(0, i -> i + 1)
                    .limit(between + 2)
                    .mapToObj(firstDate::plusSeconds)
                    .sorted()
                    .toList();
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

    public boolean writeToCSV(String line, LocalDateTime localDateTime) {
        synchronized (this) {
            while (!localDateTime.equals(firstDate)) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(pathToOutputFile, true))) {
                bw.write(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
            firstDate = firstDate.plusSeconds(1);
            this.notifyAll();
        }
        return true;
    }

    public List<String> getListOfUniqueIDs() {
        return listOfUniqueIDs;
    }

    public List<LocalDateTime> getIntervalOfDateTime() {
        return intervalOfDateTime;
    }
}
