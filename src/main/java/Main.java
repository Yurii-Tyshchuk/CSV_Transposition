import me.tongfei.progressbar.ProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static void main(String[] args) {
        String pathToInputFile = args[0];
        String pathToOutputFile = args[1];
        String pathToFileWithUniqueID = args[2];
        Resource resource = new Resource(pathToInputFile, pathToOutputFile, pathToFileWithUniqueID);

        List<String> listOfUniqueIDs = resource.getListOfUniqueIDs();
        OneSecondEntity.setListOfUniqueIDs(listOfUniqueIDs);

        Map<LocalDateTime, OneSecondEntity> mapOfDateTime = new HashMap<>();

        try (BufferedReader bufferedReader = resource.readCSV();
             BufferedReader bufferedReader2 = resource.readCSV();
             ProgressBar pb = new ProgressBar("Number of dates", bufferedReader2.lines().count())) {

            AtomicBoolean isItThirdSecond = new AtomicBoolean(false);

            bufferedReader.lines().forEachOrdered(line -> {
                String[] columns = line.split(";");
                LocalDateTime currentTime = resource.getDateTime(columns[0]);

                LocalDateTime roundTime = currentTime.truncatedTo(ChronoUnit.SECONDS);
                LocalDateTime roundTimePlusSecond = roundTime.plus(Duration.ofSeconds(1));
                LocalDateTime roundTimeMinusSecond = roundTime.minus(Duration.ofSeconds(1));

                LocalDateTime center = roundTime.plus(Duration.ofMillis(500));

                String tagName = columns[3];
                if (listOfUniqueIDs.contains(tagName)) {
                    if (!mapOfDateTime.containsKey(roundTime)) {
                        mapOfDateTime.put(roundTime, new OneSecondEntity(roundTime));
                    }

                    OneSecondEntity oneSecondEntity = mapOfDateTime.get(roundTime);
                    String value = columns[1];

                    oneSecondEntity.addValueBySensorId(tagName, Double.parseDouble(value));

                    if (resource.getFirstDate().equals(roundTime)) {
                        if (!mapOfDateTime.containsKey(roundTimePlusSecond)) {
                            mapOfDateTime.put(roundTimePlusSecond, new OneSecondEntity(roundTimePlusSecond));
                        }
                        if (currentTime.compareTo(center) > 0) {
                            mapOfDateTime.get(roundTimePlusSecond).addValueBySensorId(tagName, Double.parseDouble(value));
                        }
                    } else if (resource.getLastDate().equals(roundTime)) {
                        if (!mapOfDateTime.containsKey(roundTimeMinusSecond)) {
                            mapOfDateTime.put(roundTimeMinusSecond, new OneSecondEntity(roundTimeMinusSecond));
                        }
                        if (currentTime.compareTo(center) <= 0) {
                            mapOfDateTime.get(roundTimeMinusSecond).addValueBySensorId(tagName, Double.parseDouble(value));
                        }
                    } else {
                        if (!mapOfDateTime.containsKey(roundTimeMinusSecond)) {
                            mapOfDateTime.put(roundTimeMinusSecond, new OneSecondEntity(roundTimeMinusSecond));
                        }
                        if (!mapOfDateTime.containsKey(roundTimePlusSecond)) {
                            mapOfDateTime.put(roundTimePlusSecond, new OneSecondEntity(roundTimePlusSecond));
                        }
                        if (currentTime.compareTo(center) > 0) {
                            mapOfDateTime.get(roundTimePlusSecond).addValueBySensorId(tagName, Double.parseDouble(value));
                        }
                        if (currentTime.compareTo(center) <= 0) {
                            mapOfDateTime.get(roundTimeMinusSecond).addValueBySensorId(tagName, Double.parseDouble(value));
                        }
                    }
                    if (isItThirdSecond.get()) {
                        if (mapOfDateTime.keySet().size() >= 12) {
                            List<LocalDateTime> localDateTimeList = mapOfDateTime.keySet().stream().sorted().toList();
                            LocalDateTime stopDateTime = roundTime.minus(Duration.ofSeconds(2));
                            for (LocalDateTime time : localDateTimeList) {
                                if (time.equals(stopDateTime)) break;
                                String resultLine = mapOfDateTime.get(time).getLine();
                                resource.writeToCSV(resultLine);
                            }
                            for (LocalDateTime time : localDateTimeList) {
                                if (time.equals(stopDateTime)) break;
                                mapOfDateTime.remove(time);
                            }
                        } else if (currentTime.equals(resource.getCurrentLastDate())) {
                            List<LocalDateTime> localDateTimeList = mapOfDateTime.keySet().stream().sorted().toList();
                            for (LocalDateTime time : localDateTimeList) {
                                String resultLine = mapOfDateTime.get(time).getLine();
                                resource.writeToCSV(resultLine);
                            }
                            isItThirdSecond.set(false);
                        }
                    } else if (roundTime.equals(resource.getFirstDate().plus(Duration.ofSeconds(2)))) {
                        isItThirdSecond.set(true);
                    }

                }
                pb.step();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
