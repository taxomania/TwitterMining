package uk.ac.manchester.cs.patelt9.twitter.parse;

import java.util.Scanner;

public abstract class ScannerThread extends Thread {
    private final Scanner scanner;

    public ScannerThread() {
        this("Scanner");
    } // ScannerThread()

    public ScannerThread(final String s) {
        super(s);
        scanner = new Scanner(System.in);
    } // ScannerThread(String)

    @Override
    public final void run() {
        while (true) {
            if (scanner.nextLine().contains("exit")) {
                performTask();
                break;
            } // if
        } // while
        scanner.close();
    } // run()

    protected abstract void performTask();
} // ScannerThread
