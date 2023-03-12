package com.giannk;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MultiThreadedDownloader {

    private static final int DEFAULT_NUMBER_THREADS = 4;
    public static final String THREAD_OPT = "n";
    public static final String THREAD_LONG_OPT = "number-thread";
    public static final String URL_OPT = "url";
    public static final String URL_LONG_OPT = "download-url";

    private String downloadUrl;
    private int numberOfThread;
    private String outputFile;

    public static void main(String[] args) {
        new MultiThreadedDownloader().execute(args);
    }

    private void execute(String[] args) {
        parseArgs(args);
        execute();
    }

    public void parseArgs(String[] args) {
        Options options = new Options();
        options.addRequiredOption(URL_OPT, URL_LONG_OPT, true, "URL of file download");
        options.addOption(THREAD_OPT, THREAD_LONG_OPT, true, "Number of threads");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption(THREAD_OPT)) {
                setNumberOfThread(Integer.parseInt(cmd.getOptionValue(THREAD_OPT)));
            } else {
                setNumberOfThread(DEFAULT_NUMBER_THREADS);
            }
            setDownloadUrl(cmd.getOptionValue(URL_OPT));
            URI uri = new URI(getDownloadUrl());
            String path = uri.getPath();
            setOutputFile(new File(path).getName());
        } catch (ParseException | URISyntaxException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(getClass().getSimpleName(), options);
            System.exit(0);
        }
    }

    public void execute() {
        try {
            URL url = new URL(getDownloadUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int fileSize = conn.getContentLength();
            conn.disconnect();

            Object downloadLock = new Object();

            List<DownloadThread> threads = new ArrayList<>();
            int blockSize = fileSize / getNumberOfThread();
            for (int i = 0; i < getNumberOfThread(); i++) {
                int startByte = i * blockSize;
                int endByte = (i == getNumberOfThread() - 1) ? fileSize - 1 : startByte + blockSize - 1;
                DownloadThread thread = new DownloadThread(startByte, endByte, url, downloadLock);
                threads.add(thread);
                thread.start();
            }
            ProgressPrinter progressPrinter = new ProgressPrinter(threads, fileSize, downloadLock);
            progressPrinter.start();
            for (DownloadThread thread : threads) {
                thread.join();
            }

            try (FileOutputStream output = new FileOutputStream(getOutputFile())) {
                for (DownloadThread thread : threads) {
                    output.write(thread.getBuffer());
                }
            }

            System.out.println("Download complete!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setNumberOfThread(int numberOfThread) {
        this.numberOfThread = numberOfThread;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public int getNumberOfThread() {
        return numberOfThread;
    }

    public String getOutputFile() {
        return outputFile;
    }
}
