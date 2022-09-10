import me.tongfei.progressbar.ProgressBar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        String pathToInputFile = args[0];
        String pathToOutputFile = args[1];
        String pathToFileWithUniqueID = args[2];
        AtomicInteger atomicInt = new AtomicInteger(0);

        Resource resource = new Resource(pathToInputFile, pathToOutputFile, pathToFileWithUniqueID);
        List<LocalDateTime> intervalOfDateTime = resource.getIntervalOfDateTime();
        List<String> listOfUniqueIDs = resource.getListOfUniqueIDs();

        try (ProgressBar pb = new ProgressBar("Number of dates", intervalOfDateTime.size())) {
            for (LocalDateTime dateTime : intervalOfDateTime) {
                pb.step();
                while (true) {
                    if (atomicInt.get() < 4) {
                        atomicInt.incrementAndGet();
                        Thread task = new MyThread(dateTime, resource, listOfUniqueIDs, atomicInt);
                        task.start();
                        break;
                    }
                }
            }
        }
    }
}
