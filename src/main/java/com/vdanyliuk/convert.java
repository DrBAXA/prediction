import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class convert {

    public static final Pattern HOURS = Pattern.compile("(\\d{1,2})ч");
    public static final Pattern MINUTES = Pattern.compile("(\\d{1,2})м");
    public static final Pattern SECONDS = Pattern.compile("(\\d{1,2})с");

    public static final Pattern PRECENTS = Pattern.compile("^(\\d+)%?");

    public static final DateTimeFormatter F1 = DateTimeFormatter.ofPattern("yyyy.M.d");
    public static final DateTimeFormatter F2 = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter F3 = DateTimeFormatter.ofPattern("dd.MM.yyyy kk:mm");

    public static void main(String[] args) throws IOException {
        Map<LocalDate, Integer> load = Files.lines(Paths.get("load.csv"))
                .map(l -> l.split(","))
                .collect(HashMap<LocalDate, Integer>::new, (m, a) -> m.put(LocalDate.parse(a[0], F2), Integer.valueOf(a[1])), HashMap::putAll);

        Map<LocalDate, String> wether = normalizeWether();

        Files.write(Paths.get("out.csv"), wether.keySet().stream().sorted()
                .filter(load::containsKey)
                .map(d -> F1.format(d) + "," +load.get(d) + "," + wether.get(d))
                .collect(Collectors.toList()));
    }

    public static Map<LocalDate, String> normalizeWether() throws IOException {
        return Files.lines(Paths.get("weather.csv"))
                .skip(1)
                .map(l -> l.split(","))
                .collect(HashMap<LocalDate, String>::new, (m, a) -> m.put(LocalDate.parse(a[0], F1), String.join(",", Arrays.copyOfRange(a, 1, a.length))), HashMap::putAll);
    }

}
