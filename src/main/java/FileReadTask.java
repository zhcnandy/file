import entity.DataEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class FileReadTask implements Callable<List<DataEntity>> {

    private String filename;
    private String name;

    public FileReadTask(String filename, String name){
        this.filename = filename;
        this.name = name;
    }

    @Override
    public List<DataEntity> call() throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
        List<DataEntity> dataEntities = lines.stream()
                .map(FileSort::parsingJson)
                .filter(e -> name.equals(e.getName())).toList();
        return dataEntities;
    }
}
