import java.io.BufferedReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MyThread extends Thread {
    private final LocalDateTime dateTime;
    private final Resource resource;
    private final List<String> listOfUniqueIDs;
    private final Map<String, List<Double>> mapOfColumns;
    private final AtomicInteger atomicInteger;

    public MyThread(LocalDateTime dateTime, Resource resource, List<String> listOfUniqueIDs, AtomicInteger atomicInteger) {
        this.dateTime = dateTime;
        this.resource = resource;
        this.listOfUniqueIDs = listOfUniqueIDs;
        this.mapOfColumns = new HashMap<>();
        this.atomicInteger = atomicInteger;
    }

    @Override
    public void run() {
        try (BufferedReader bufferedReader = resource.readCSV()) {
            LocalDateTime minusOneSecond = dateTime.minus(Duration.ofMillis(500));
            LocalDateTime plusOneSecond = dateTime.plus(Duration.ofMillis(500));
            String line;
            boolean flag = false;
            while ((line = bufferedReader.readLine()) != null) {
                String[] columns = line.split(";");
                LocalDateTime currentTime = resource.getDateTime(columns[0]);


                if (minusOneSecond.compareTo(currentTime) <= 0 && plusOneSecond.compareTo(currentTime) >= 0) {
                    System.out.println(Thread.currentThread() + "currentTime " + currentTime + " minusOneSecond " + minusOneSecond + " plusOneSecond " + plusOneSecond);
                    flag = true;
                    String value = columns[1];
                    String tagName = columns[3];
                    mapOfColumns.computeIfAbsent(tagName, s -> new ArrayList<>());
                    List<Double> doubleList = mapOfColumns.get(tagName);
                    doubleList.add(Double.valueOf(value));
                } else if (flag) {
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        StringBuilder resultLine = new StringBuilder();

        resultLine.append(dateTime);
        resultLine.append(";");

        for (String id : listOfUniqueIDs) {
            if (mapOfColumns.containsKey(id)) {
                double avg = mapOfColumns.get(id)
                        .stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElseThrow(IllegalStateException::new);
                resultLine.append(avg);
                resultLine.append(";");
            } else {
                resultLine.append(";");
            }
        }
        resultLine.append("\n");
        if (resource.writeToCSV(resultLine.toString(), dateTime)) {
            atomicInteger.decrementAndGet();
        }
    }
}
