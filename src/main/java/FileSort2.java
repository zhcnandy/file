import entity.DataEntity;
import utils.DateUtils;
import utils.JsonUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSort2 {
    static String path = "D:\\tmp\\records";
    static String name = "吴小明";
    static String outputBase = "D:\\tmp\\output\\";

    static Integer size = 10000;
    static Integer readTask = 3;

    static AtomicInteger index = new AtomicInteger(0);


    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        long start = System.currentTimeMillis();
        String basePath = outputBase + name;
        String suffix = ".txt";

        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("corePoolSize:" + processors);
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(10);
        ThreadPoolExecutor poll = new ThreadPoolExecutor(processors, processors,
                60L, TimeUnit.SECONDS, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());
        ThreadPoolExecutor readPoll = new ThreadPoolExecutor(processors, processors,
                60L, TimeUnit.SECONDS, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());


        List<String> fileNames = getAllFileNames(path);
        //System.out.println(fileNames);

        LinkedList<String> linkedList = new LinkedList<>(fileNames);
        ConcurrentLinkedQueue<DataEntity> result = new ConcurrentLinkedQueue<>();

        Future<List<DataEntity>>[] futures = new Future[readTask];
        while (linkedList.size() > readTask){

            for (int j = 0; j < futures.length; j++) {
                String fileName = linkedList.pop();
                Future<List<DataEntity>> submit = readPoll.submit(new FileReadTask(fileName, name));
                futures[j] = submit;
            }
            for (int j = 0; j < futures.length; j++) {
                result.addAll(futures[j].get());
            }


            if (result.size() > size){
                ConcurrentLinkedQueue<DataEntity> tmp = result;
                result = new ConcurrentLinkedQueue<>();
                saveLines(tmp, basePath, suffix, poll);
            }
        }

        while (linkedList.size() > 0){
            String fileName = linkedList.pop();
            Future<List<DataEntity>> submit = readPoll.submit(new FileReadTask(fileName, name));
            result.addAll(submit.get());
        }

        if (result.size() > 0) {
            saveLines(result, basePath, suffix, poll);
        }

        System.out.println("map file:"+ (System.currentTimeMillis() - start) + "ms");
        readPoll.shutdown();
        poll.shutdown();

        List<String> strings = Files.walk(Paths.get(basePath))
                .filter(Files::isDirectory)
                .filter(p -> !basePath.equals(p.toString()))
                .map(Path::toString).toList();

        for (String baseDirectory : strings) {
            TreeSet<DataEntity> set = new TreeSet<>(new DataEntityComparator());
            List<String> allFileNames = getAllFileNames(baseDirectory);

            for (String fileName : allFileNames) {
                Path filePath = Paths.get(fileName);
                List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
                List<DataEntity> dataEntities = lines.stream().map(FileSort2::parsingJson).toList();
                set.addAll(dataEntities);

                filePath.toFile().delete();
            }
            Paths.get(baseDirectory).toFile().delete();

            DataEntity entity = set.stream().findFirst().get();
            String context = set.stream().map(DataEntity::getLine).collect(Collectors.joining("\n"));

            Path of = Path.of(basePath, entity.getDate() + suffix);
            try (FileWriter fileWriter = new FileWriter(of.toFile(), false)){
                fileWriter.append(context);
                fileWriter.flush();
            }
        }

        System.out.println("merge file:"+ (System.currentTimeMillis() - start) + "ms");
    }

    private static void saveLines(Collection<DataEntity> lines, String basePath, String suffix, ThreadPoolExecutor poll){
        Map<String, List<DataEntity>> listMap = lines.stream().parallel()
                .collect(Collectors.groupingByConcurrent(DataEntity::getDate, Collectors.toList()));

        for (Map.Entry<String, List<DataEntity>> stringListEntry : listMap.entrySet()) {
            DataEntity[] dataEntities = stringListEntry.getValue().toArray(new DataEntity[0]);
            FileReduceTask fileReduceTask = new FileReduceTask(stringListEntry.getKey(), index.getAndAdd(1), basePath, suffix, dataEntities);
            poll.submit(fileReduceTask);
        }
    }

    private static List<String> readAllLines(String fileName) {
        try {
            return Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public static DataEntity parsingJson(String line) {
        DataEntity entity = JsonUtils.fromJson(line, DataEntity.class);
        entity.setLine(line);
        entity.setDate(DateUtils.ofEpochMilliToDate(entity.getTimestamp()));
        return entity;
    }


    private static List<String> getAllFileNames(String directory) {
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            return paths.filter(Files::isRegularFile)
                    .map(Path::toString)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
