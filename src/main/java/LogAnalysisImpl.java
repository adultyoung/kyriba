import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Thread.MAX_PRIORITY;

public class LogAnalysisImpl implements LogAnalysis {
    private static HashMap<List<String>, Integer> output;

    static void addToOutput(List<String> cell) {
        if (output.containsKey(cell)) {
            output.put(cell, output.get(cell) + 1);
        } else {
            output.put(cell, 1);
        }
    }

    @Override
    public void filterLogs(FilterParameters filterParameters, GroupingParameters groupingParameters, int countOfThreads, String outputPath) {
        output = new HashMap<>();
        countOfThreads = countOfThreads < 1 ? 1 : countOfThreads;
        List<Path> files;
        try {
            files = Files.walk(Paths.get(Variables.inputPath))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            countOfThreads = countOfThreads > files.size()-1 ? files.size()-1 : countOfThreads;
            int filesInOneThread = (files.size() +1) / countOfThreads;
            ExecutorService threadPool = Executors.newFixedThreadPool(countOfThreads);
            for (int i = 0; i < countOfThreads; i++) {
                Thread thread;
                if (i == countOfThreads-1) {
                    thread = new Thread(new LogSearch(filterParameters, groupingParameters.getUsername(), groupingParameters.getTimeUnit(), files.subList(i * filesInOneThread, files.size())));
                } else {
                thread = new Thread(new LogSearch(filterParameters, groupingParameters.getUsername(), groupingParameters.getTimeUnit(), files.subList(i * filesInOneThread, (i + 1) * filesInOneThread)));
                }
                threadPool.execute(thread);
            }
            threadPool.shutdown();
            threadPool.awaitTermination(MAX_PRIORITY, TimeUnit.HOURS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        writeToFile(outputPath, groupingParameters.getUsername(), groupingParameters.getTimeUnit());
    }

    private void writeToFile(String outputPath, String username, ChronoUnit timeUnit) {
        File file = new File(outputPath);
        if (file.isDirectory()) {
            outputPath = file.getPath() + "\\output";
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputPath));
            String format = "%20s %20s \r\n";
            String header;
            if ("".equals(username) || username == null) {
                header = timeUnit.toString();
            } else if (timeUnit == null) {
                header = username;
            } else {
                header = username + ", " + timeUnit.toString();
            }
            bufferedWriter.write(String.format(format, header, "Count of Records"));
            output.forEach((key, value) -> {
                String leftCell = "";
                for (String cell : key) {
                    leftCell += cell;
                }
                try {
                    bufferedWriter.write(String.format(format, leftCell, value));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
