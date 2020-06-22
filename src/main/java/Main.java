import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        LogAnalysis logAnalysis = new LogAnalysisImpl();
        FilterParameters filterParameters = new FilterParameters();
        filterParameters.setUsername("username");
//        filterParameters.setStartOfPeriod(LocalDateTime.now());
//        filterParameters.setEndOfPeriod(LocalDateTime.now().minusDays(5));
        GroupingParameters groupingParameters = new GroupingParameters();
        groupingParameters.setTimeUnit(ChronoUnit.MINUTES);
        logAnalysis.filterLogs(filterParameters,groupingParameters,3,"output");
    }
}
