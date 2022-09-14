import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneSecondEntity {
    private final LocalDateTime currentTime;
    private final Map<String, List<Double>> columns = new HashMap<>();
    private static List<String> listOfUniqueIDs;

    public OneSecondEntity(LocalDateTime currentTime) {
        this.currentTime = currentTime;
    }

    public void addValueBySensorId(String id, double value) {
        List<Double> values;
        if (!columns.containsKey(id)) {
            columns.put(id, new ArrayList<>());
        }
        values = columns.get(id);
        assert values != null;
        values.add(value);
    }

    public String getLine() {
        StringBuilder result = new StringBuilder();
        result.append(currentTime);
        result.append(";");
        for (String id : listOfUniqueIDs) {
            if (columns.containsKey(id)) {
                double avg = columns.get(id)
                        .stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElseThrow(IllegalStateException::new);
                result.append(avg);
                result.append(";");
            } else {
                result.append(";");
            }
        }
        result.append("\n");
        return result.toString();
    }

    public static void setListOfUniqueIDs(List<String> listOfUniqueIDs) {
        OneSecondEntity.listOfUniqueIDs = listOfUniqueIDs;
    }
}
