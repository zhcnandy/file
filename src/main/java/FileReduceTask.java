import entity.DataEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FileReduceTask implements Runnable {

    private String dateStr;
    private String basePath;
    private String suffix;
    private Integer index;

    private DataEntity[] dataEntities;

    public FileReduceTask(String date, Integer index, String basePath, String suffix, DataEntity[] dataEntities) {
        this.dateStr = date;
        this.basePath = basePath;
        this.suffix = suffix;
        this.index = index;
        this.dataEntities = dataEntities;
    }

    @Override
    public void run() {
        Arrays.sort(dataEntities, new DataEntityComparator());

        Path path = Paths.get(basePath, dateStr);
        File file = path.toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        path = Paths.get(basePath, dateStr, index + suffix);
        file = path.toFile();
        try (FileWriter fileWriter = new FileWriter(file, true);) {
            String context = Arrays.stream(dataEntities)
                    .map(DataEntity::getLine)
                    .collect(Collectors.joining("\n"));

            fileWriter.append(context);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
