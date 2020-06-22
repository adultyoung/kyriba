import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LogSearch implements Runnable {

    private final FilterParameters filterParameters;
    private final String username;
    private final ChronoUnit unit;
    private final List<Path> files;
    private final Pattern logPattern = Pattern.compile("\\s");

    public LogSearch(FilterParameters filterParameters, String username, ChronoUnit unit, List<Path> files) {
        this.filterParameters = filterParameters;
        this.username = username;
        this.unit = unit;
        this.files = files;
    }

    @Override
    public void run() {

        for (Path file : files) {
            try {
                LineIterator lineIterator = FileUtils.lineIterator(file.toFile(), "UTF-8");
                while (lineIterator.hasNext()) {
                    String line = lineIterator.next();
                    String[] cells = logPattern.split(line, 3);
                    if (validateLine(cells)) addToOutput(cells);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addToOutput(String[] cells) {
        List<String> headers = new ArrayList<>();
        if (!"".equals(username) && username != null) headers.add(username);
        LocalDateTime parse = LocalDateTime.parse(cells[0], DateTimeFormatter.ofPattern("yyyy/MM/dd_HH:mm"));
        if (unit != null) switch (unit) {
            case YEARS:
                headers.add(parse.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy")));
                break;
            case MONTHS:
                headers.add(parse.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy/MM")));
                break;
            case DAYS:
                headers.add(parse.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
                break;
            case HOURS:
                headers.add(parse.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd_HH")));
                break;
            case MINUTES:
                headers.add(parse.format(DateTimeFormatter.ofPattern("yyyy/MM/dd_HH:mm")));
                break;
        }
        LogAnalysisImpl.addToOutput(headers);
    }

    private boolean validateLine(String[] cells) {
        LocalDateTime timeInCell = LocalDateTime.parse(cells[0], DateTimeFormatter.ofPattern("yyyy/MM/dd_HH:mm"));
        if (filterParameters.getStartOfPeriod() != null && filterParameters.getEndOfPeriod() != null) {
            if (timeInCell.isBefore(filterParameters.getStartOfPeriod()) || !timeInCell.isBefore(filterParameters.getEndOfPeriod()))
                return false;

        }
        if (!cells[1].equals(filterParameters.getUsername())) return false;
        if (filterParameters.getMessagePattern() != null) {
            return cells[2].matches(filterParameters.getMessagePattern());
        }
        return true;
    }
}
