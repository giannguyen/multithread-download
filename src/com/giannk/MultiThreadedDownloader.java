package com.giannk;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MultiThreadedDownloader {
    private final static int NUM_THREADS = 4;
    private final static int BUFFER_SIZE = 1024 * 1024;

    public static void main(String[] args) throws Exception {
        String fileUrl = "https://raw.githubusercontent.com/zhaofei01/book/master/Java/Effective%20Java%2C%20Third%20Edition.pdf";
        String outputFilePath = "abc.pdf";

        URL url = new URL(fileUrl);
        long fileSize = url.openConnection().getContentLengthLong();
        File outputFile = new File(outputFilePath);

        List<DownloadThread> threads = new ArrayList<>();
        Object progressMonitor = new ProcessPrintThread();

        for (int i = 0; i < NUM_THREADS; i++) {
            DownloadThread thread = new DownloadThread(fileUrl, outputFilePath + "." + i, BUFFER_SIZE, fileSize, progressMonitor);
            threads.add(thread);
            thread.start();
        }

        for (DownloadThread thread : threads) {
            thread.join();
        }

        mergeFiles(outputFilePath, outputFile.length(), NUM_THREADS);
    }

    private static void mergeFiles(String outputFilePath, long fileSize, int numThreads) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        try (FileOutputStream outputStream = new FileOutputStream(outputFilePath)) {
            for (int i = 0; i < numThreads; i++) {
                try (BufferedInputStream inputStream = new BufferedInputStream(new java.io.FileInputStream(outputFilePath + "." + i))) {
                    while ((bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                new File(outputFilePath + "." + i).delete();
            }
        }
        System.out.println("Download completed!");
    }
}
