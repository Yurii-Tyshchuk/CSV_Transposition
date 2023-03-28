import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

public class Resource {
    private final String pathToInputFile;
    private final String pathToOutputFile;
    private final String pathToFileWithUniqueID;
    private static LocalDateTime firstDate;
    private final List<String> listOfUniqueIDs = new ArrayList<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss.SSS");
    private List<LocalDateTime> localDateTimeListBetweenFirstAndLastDate;

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

            createHeader();

            BufferedReader inputFile = new BufferedReader(new FileReader(pathToInputFile));

            inputFile.readLine();
            String firstDateTime = inputFile.readLine().split(";")[0];
            String lastDateTime = "";

            while ((line = inputFile.readLine()) != null)
                lastDateTime = line;
            lastDateTime = lastDateTime.split(";")[0];
            inputFile.close();

            LocalDateTime firstDate = getDateTime(firstDateTime).truncatedTo(ChronoUnit.SECONDS);
            Resource.firstDate = getDateTime(firstDateTime).truncatedTo(ChronoUnit.SECONDS);
            LocalDateTime lastDate = getDateTime(lastDateTime).truncatedTo(ChronoUnit.SECONDS);

            localDateTimeListBetweenFirstAndLastDate = generateLocalDateTimeListBetweenDates(firstDate, lastDate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<LocalDateTime, OneRow> getListOfRows() {
        HashMap<LocalDateTime, OneRow> result = new HashMap<>();
        for (LocalDateTime localDateTime : localDateTimeListBetweenFirstAndLastDate) {
            result.put(localDateTime, new OneRow(localDateTime));
        }
        return result;
    }

    private List<LocalDateTime> generateLocalDateTimeListBetweenDates(LocalDateTime firstDate, LocalDateTime lastDate) {
        long numOfSecondBetween = ChronoUnit.SECONDS.between(firstDate, lastDate);

        return IntStream.iterate(0, i -> i + 1)
                .limit(numOfSecondBetween + 1)
                .mapToObj(firstDate::plusSeconds)
                .toList();
    }

    public LocalDateTime getDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime.substring(0, dateTime.length() - 10), formatter);
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

    public OutputStreamWriter getOutputStreamWriter() throws FileNotFoundException {
        return new OutputStreamWriter(new FileOutputStream(pathToOutputFile, true), StandardCharsets.UTF_8);
    }

    private void createHeader() {
        StringBuilder result = new StringBuilder("Timestamp;");
        for (String id : listOfUniqueIDs) {
            result.append(id);
            result.append(";");
        }
        result.setLength(result.length() - 1);
        result.append("\n");
        try (OutputStreamWriter bw = new OutputStreamWriter(new FileOutputStream(pathToOutputFile, true), StandardCharsets.UTF_8)) {
            bw.write(result.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getListOfUniqueIDs() {
        return listOfUniqueIDs;
    }

    public static LocalDateTime getFirstDate() {
        return firstDate;
    }
}
