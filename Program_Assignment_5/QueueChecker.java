package Program_Assignment_5;
import java.util.*;
import static java.lang.Thread.sleep;

class QueueChecker {

    LinkedList<Integer> queueA = new LinkedList<>();
    LinkedList<Integer> queueB = new LinkedList<>();
    LinkedList<Integer> queueC = new LinkedList<>();

    private int capacityAB = 20; // wait comparator for populateBy*() methods
    private int numPopulated = 0; // exit comparator for Populater thread
    private int numProcessedA = 0; // accumulates number processed by queueA
    private int numProcessedB = 0; // accumulates number processed by queueB
    private int numProcessedC = 0; // exit comparator for queueC
    private int totalTimeA; // accumulates queueA total processing time
    private int totalTimeB; // accumulates queueB total processing time
    private int avgTimeA; // (totalTimeA / numProcessedA)
    private int avgTimeB; // (totalTimeB / numProcessedB)

    // MAX_POPULATE_INTERVAL is class var
    // since used in each of my populate methods,
    // is 5x faster than (local process() var) MAX_PROCESS_INTERVAL
    // so that queue will build while processing
    private final int MAX_POPULATE_INTERVAL = 10;


    // Populates people into queues by turn
    void populateByTurn() throws InterruptedException {
        LinkedList<Integer> priorityQueue;
        boolean turn = true;

        while (numPopulated < 50) {
            synchronized (this) {

                if(turn) priorityQueue = queueA;
                else priorityQueue = queueB;
                turn = !turn; // flip for next turn

                // Waits to populate while priorityQueue is full
                while (priorityQueue.size() == capacityAB) {
                    System.out.println(Thread.currentThread().getName()
                            + " at capacity...");
                    wait();
                }

                // Add next person to priorityQueue
                // sleep simulates random arrival
                sleep(new Random().nextInt(MAX_POPULATE_INTERVAL));
                priorityQueue.add(numPopulated);
                if(turn)
                    System.out.println("queueA populated: " + numPopulated++);
                else
                    System.out.println("queueB populated: " + numPopulated++);

                notify();
            }
        }
    }

    // Here people populate into the smallest queue
    void populateBySize() throws InterruptedException {
        LinkedList<Integer> priorityQueue;

        while (numPopulated < 50) {
            synchronized (this) {

                if (queueA.size() < queueB.size()) priorityQueue = queueA;
                else priorityQueue = queueB;

                // Waits to populate while priorityQueue is full
                while (priorityQueue.size() == capacityAB) {
                    System.out.println(Thread.currentThread().getName() +
                            " at capacity...");
                    wait();
                }

                // Add next person to priorityQueue
                sleep(new Random().nextInt(MAX_POPULATE_INTERVAL));
                priorityQueue.add(numPopulated);

                if (priorityQueue == queueA)
                    System.out.println("queueA populated: " + numPopulated++);
                else System.out.println("queueB populated: " + numPopulated++);

                notify();
            }
        }
    }

    // Gives priority to the queue with the historically faster processing
    void populateBySpeed() throws InterruptedException {
        LinkedList<Integer> priorityQueue;
        boolean turn = true;

        while (numPopulated < 50) {
            synchronized (this) {

                // If same, no speed priority given
                // default to turn priority
                if (avgTimeA == avgTimeB) {
                    if(turn) priorityQueue = queueA;
                    else priorityQueue = queueB;
                    turn = !turn; // flip for next turn
                }
                else if (avgTimeA < avgTimeB) priorityQueue = queueA;
                else priorityQueue = queueB;

                // Waits to populate while priorityQueue is full
                while (priorityQueue.size() == capacityAB) {
                    System.out.println(Thread.currentThread().getName() +
                            " at capacity...");
                    wait();
                }

                // Add next person to priorityQueue
                sleep(new Random().nextInt(MAX_POPULATE_INTERVAL));
                priorityQueue.add(numPopulated);

                if (priorityQueue == queueA)
                    System.out.println("queueA populated: " + numPopulated++);
                else System.out.println("queueB populated: " + numPopulated++);

                notify();
            }
        }
    }

    void process(LinkedList<Integer> checkingQueue) throws InterruptedException {
        final int MAX_PROCESS_INTERVAL = 50;

        while (true) {
            synchronized (this) {
                // exit when checkingQueue is empty and no more to process
                if(checkingQueue.peek() == null) {
                    if( (checkingQueue == queueC && numProcessedC == 50) ||
                            (checkingQueue != queueC && (numProcessedA + numProcessedB) == 50) )
                        break;
                    else continue;
                }
                if(checkingQueue == queueC){ // final queue
                    sleep(new Random().nextInt(MAX_PROCESS_INTERVAL));
                    System.out.println(Thread.currentThread().getName() +
                            " processed person: " + checkingQueue.removeFirst());
                    numProcessedC++;
                }
                else { // if queueA or queueB

                    // queueC allows twice the queue size as AB
                    // since C feeds from both
                    while (queueC.size() == 2*capacityAB) {
                        System.out.println(Thread.currentThread().getName() +
                                " is waiting...");
                        wait();
                    }

                    int time = new Random().nextInt(MAX_PROCESS_INTERVAL);
                    sleep(time); // simulates processing times

                    int person = checkingQueue.removeFirst();
                    queueC.add(person);
                    System.out.println(Thread.currentThread().getName() +
                            " processed person: " + person);

                    if(checkingQueue == queueA) {
                        ++numProcessedA;
                        totalTimeA += time;
                        avgTimeA = totalTimeA/numProcessedA;
                    }
                    else if(checkingQueue == queueB) {
                        ++numProcessedB;
                        totalTimeB += time;
                        avgTimeB = totalTimeB/numProcessedB;
                    }
                }
                notify();
            }
        }
    }
}
