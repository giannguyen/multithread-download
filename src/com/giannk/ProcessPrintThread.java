package com.giannk;

public class ProcessPrintThread {

    private final static int PROGRESS_BAR_WIDTH = 50;

    public synchronized void updateProgress(long bytesDownloaded, long fileSize) {
        double percentComplete = (double) bytesDownloaded / fileSize;
        int progressBarLength = (int) (percentComplete * PROGRESS_BAR_WIDTH);
        String progressBar = "[" + "=".repeat(progressBarLength) + " ".repeat(PROGRESS_BAR_WIDTH - progressBarLength) + "]";
        System.out.printf("\r%s %d - %s/%s", progressBar, (int) (percentComplete * 100), formatSize(bytesDownloaded), formatSize(fileSize));
        if (bytesDownloaded == fileSize) {
            System.out.println();
            notifyAll();
        }
    }

    private String formatSize(long bytes) {
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.1f %s", size, units[unitIndex]);
    }
}