package cz.deefeen.hardcore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StatsManager {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final File file;
    private final Gson gson;
    private final List<RunEntry> runs = new ArrayList<>();
    private int nextRunId = 1;
    private int wipeCount = 0;

    public StatsManager(File dataFolder) {
        this.file = new File(dataFolder, "stats.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        load();
    }

    private void load() {
        if (!file.exists()) {
            runs.clear();
            nextRunId = 1;
            wipeCount = 0;
            return;
        }
        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<StatsData>() {}.getType();
            StatsData data = gson.fromJson(reader, type);
            if (data != null) {
                runs.clear();
                if (data.runs != null) {
                    runs.addAll(data.runs);
                }
                nextRunId = data.nextRunId;
                wipeCount = data.wipeCount;
            }
        } catch (IOException e) {
            runs.clear();
            nextRunId = 1;
            wipeCount = 0;
        }
    }

    public void save() {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(new StatsData(nextRunId, wipeCount, runs), writer);
        } catch (IOException e) {
            // silent
        }
    }

    public void addRun(String playerName, String deathMessage) {
        runs.add(new RunEntry(
                nextRunId++,
                playerName,
                deathMessage,
                LocalDateTime.now().format(FORMATTER)
        ));
        save();
    }

    public List<RunEntry> getRuns() {
        return new ArrayList<>(runs);
    }

    public int getWipeCount() {
        return wipeCount;
    }

    public void incrementWipeCount() {
        wipeCount++;
        save();
    }

    public void resetWipeCount() {
        wipeCount = 0;
        save();
    }

    private static class StatsData {
        int nextRunId;
        int wipeCount;
        List<RunEntry> runs;

        StatsData() {
        }

        StatsData(int nextRunId, int wipeCount, List<RunEntry> runs) {
            this.nextRunId = nextRunId;
            this.wipeCount = wipeCount;
            this.runs = runs;
        }
    }
}
