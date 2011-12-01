package uk.ac.manchester.cs.patelt9.twitter.parse;

import java.io.IOException;
import java.util.NoSuchElementException;
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
        while (!isInterrupted()) {
            try {
                if (scanner.nextLine().contains("exit")) {
                    interrupt();
                } // if
            } catch (final NoSuchElementException e) {
                // Hacky Method
            } // catch
        } // while
    } // run()

    @Override
    public void interrupt() {
        performTask();
        try {
            System.in.close();
        } catch (final IOException e) {
            // Hacky Method
        } // catch
        scanner.close();
        super.interrupt();
    } // close()

    protected abstract void performTask();
} // ScannerThread
