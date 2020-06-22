public interface LogAnalysis {

    void filterLogs(FilterParameters filterParameters, GroupingParameters groupingParameters, int countOfThreads, String outputPath);

}
