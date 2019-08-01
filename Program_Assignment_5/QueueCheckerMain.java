package Program_Assignment_5;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

public class QueueCheckerMain {
    public static void main(String[] args) throws InterruptedException {
        QueueChecker qc = new QueueChecker();

        class Populater implements Runnable{
            private int mode;

            private Populater(int mode){
                this.mode = mode;
            }

            @Override
            public void run() {
                try {
                    if (mode == 1) qc.populateByTurn();
                    else if (mode == 2) qc.populateBySize();
                    else if (mode == 3) qc.populateBySpeed();
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        class Checker implements Runnable {
            private LinkedList<Integer> checkingQueue;

            private Checker(LinkedList<Integer> checkingQueue){
                this.checkingQueue = checkingQueue;
            }

            @Override
            public void run() {
                try {
                    qc.process(checkingQueue);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // All populate from common population
        Thread Populater = new Thread(new Populater(3), "Populater");

        // All checked separately
        Thread CheckerA = new Thread(new Checker(qc.queueA), "CheckerA");
        Thread CheckerB = new Thread(new Checker(qc.queueB), "CheckerB");
        Thread CheckerC = new Thread(new Checker(qc.queueC), "CheckerC");

        Long start = System.currentTimeMillis();

        Populater.start();
        CheckerA.start();
        CheckerB.start();
        CheckerC.start();

        CheckerC.join(); //waits for final process before reporting time

        Long timeElapsed = (System.currentTimeMillis() - start);
        System.out.println("\nTime Elapsed: " + timeElapsed + " ms");


        try(FileWriter fw = new FileWriter("timeData.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(timeElapsed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
