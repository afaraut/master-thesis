package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import static utils.Toolbox.humanReadableByteCount;

/**
 * Created by Anthony on 13/04/2016.
 */
public class SystemInfo {
    public enum Moment {START, END}

    /**
     * Retrieve info from the system concerning the memory usage and the time T for calculate a delta
     *
     * @param m The instant T, START or END
     * @return The Runtime containing some information from the system
     */
    public static RuntimeInfo info(Moment m) {
        Runtime runtime = Runtime.getRuntime();
        if (m == Moment.START) {
            runtime.gc(); // Call the garbage collector
        }
        return new RuntimeInfo(runtime.maxMemory(), runtime.totalMemory(), runtime.freeMemory(), System.currentTimeMillis());
    }

    /**
     * Write in a file all the information concerning the memory usage and the delta time from end to start
     *
     * @param start    The RuntimeInfo before the code
     * @param end      The RuntimeInfo after the code
     * @param text     A text in order to specify the stage
     * @param namefile The path for the file
     */
    public static void info_memo_usage_time(RuntimeInfo start, RuntimeInfo end, String text, String namefile) {
        StringBuilder sb = new StringBuilder();
        sb.append("START -------------------------------------------------------------------- " + text + "\n");

        long start_maxMemory = start.get_maxMemory();
        long start_allocatedMemory = start.get_allocatedMemory();
        long start_freeMemory = start.get_freeMemory();
        long start_currentTimeMillis = start.get_currentTimeMillis();
        long end_maxMemory = end.get_maxMemory();
        long end_allocatedMemory = end.get_allocatedMemory();
        long end_freeMemory = end.get_freeMemory();
        long end_currentTimeMillis = end.get_currentTimeMillis();

        sb.append("\n\t ");
        sb.append("Free memory: ");
        sb.append(humanReadableByteCount(start_freeMemory, true));
        sb.append(" - ");
        sb.append("or " + start_freeMemory + " bytes");
        sb.append("\n\t");
        sb.append("Allocated memory: ");
        sb.append(humanReadableByteCount(start_allocatedMemory, true));
        sb.append(" - ");
        sb.append("or " + start_allocatedMemory + " bytes");
        sb.append("\n\t");
        sb.append("Max memory: ");
        sb.append(humanReadableByteCount(start_maxMemory, true));
        sb.append(" - ");
        sb.append("or " + start_maxMemory + " bytes");
        sb.append("\n\t");
        sb.append("Total free memory: ");
        sb.append(humanReadableByteCount(start_freeMemory + (start_maxMemory - start_allocatedMemory), true));
        sb.append(" - ");
        sb.append("or " + (start_freeMemory + (start_maxMemory - start_allocatedMemory)) + " bytes");
        sb.append("\n");
        sb.append("\t\t\t ------------------------ \n");
        sb.append("\n\t");
        sb.append("Free memory: ");
        sb.append(humanReadableByteCount(end_freeMemory, true));
        sb.append(" - ");
        sb.append("or " + end_freeMemory + " bytes");
        sb.append("\n\t");
        sb.append("Allocated memory: ");
        sb.append(humanReadableByteCount(end_allocatedMemory, true));
        sb.append(" - ");
        sb.append("or " + end_allocatedMemory + " bytes");
        sb.append("\n\t");
        sb.append("Max memory: ");
        sb.append(humanReadableByteCount(end_maxMemory, true));
        sb.append(" - ");
        sb.append("or " + end_maxMemory + " bytes");
        sb.append("\n\t");
        sb.append("Total free memory: ");
        sb.append(humanReadableByteCount(end_freeMemory + (end_maxMemory - end_allocatedMemory), true));
        sb.append(" - ");
        sb.append("or " + (end_freeMemory + (end_maxMemory - end_allocatedMemory)) + " bytes");
        sb.append("\n");

        sb.append("\nEND -------------------------------------------------------------------- " + text + "\n");

        long start_calcul = start_allocatedMemory - start_freeMemory;
        long end_calcul = end_allocatedMemory - end_freeMemory;

        sb.append("\t/!\\ USAGE of memory " + humanReadableByteCount(end_calcul - start_calcul, true) + " - or " + (end_calcul - start_calcul) + " bytes\n");
        sb.append("\t/!\\ TIME " + new SimpleDateFormat("mm'm ':' 'ss's ':' 'SSS'ms'").format(new Date(end_currentTimeMillis - start_currentTimeMillis)) + " - or " + (end_currentTimeMillis - start_currentTimeMillis) + "ms");

        try {
            Files.write(Paths.get(namefile), sb.toString().getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}