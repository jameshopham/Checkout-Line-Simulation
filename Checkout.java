/* Group 4 
 * Alexander Bautista, Boshra Alzindeni, James Hopham, Nasir Ali, Nguyen Truong
 * May 24, 2024
 * This program uses three different models (single line, assign customers to smallest line, 
 * or assign customers to a random line) for self-checkout stations to simulate metrics
 * used for decision making to determine the best approach to use.  
 */

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.Random;
import java.util.Comparator;

class Customer {
    int arrivalTime;
    int numItems;
    int startCheckoutTime;

    public Customer(int arrivalTime, int numItems) {
        this.arrivalTime = arrivalTime;
        this.numItems = numItems;
        this.startCheckoutTime = -1;
    }

    public int getCheckoutDuration() {
        return (numItems * 5) + (20 + new Random().nextInt(21)); // 5 seconds per item + 20-40 seconds to pay
    }
}

class CheckoutStation {
    boolean busy;
    int freeTime;
    Customer currentCustomer;

    public CheckoutStation() {
        this.busy = false;
        this.freeTime = 0;
        this.currentCustomer = null;
    }

    public void startCheckout(Customer customer, int currentTime) {
        this.currentCustomer = customer;
        customer.startCheckoutTime = currentTime;
        this.freeTime = currentTime + customer.getCheckoutDuration();
        this.busy = true;
    }

    public void update(int currentTime) {
        if (busy && currentTime >= freeTime) {
            busy = false;
            currentCustomer = null;
        }
    }

    public boolean isFree() {
        return !busy;
    }
}

class Queue<T> {
    ArrayList<T> items;

    public Queue() {
        this.items = new ArrayList<>();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public void enqueue(T item) {
        this.items.add(0, item);
    }

    public T dequeue() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Queue is empty.");
        }
        return this.items.remove(this.size() - 1);
    }

    public T peek() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Queue is empty.");
        }
        return this.items.get(this.size() - 1);
    }

    public int size() {
        return this.items.size();
    }

    public String toString() {
        if (!this.items.isEmpty()) {
            String arrString = this.items.toString();
            return "tail ->" + arrString + "-> head";
        } else {
            return "<<empty queue>>";
        }
    }
}

class Simulation {
    int numStations;
    int simulationDuration;
    int customerArrivalRate;
    List<CheckoutStation> stations;
    List<Queue<Customer>> queues;
    Queue<Customer> queue;
    Random random;
    int totalCustomersServed;
    int totalWaitTime;
    int maxQueueLength;

    public Simulation(int numStations, int simulationDuration, int customerArrivalRate) {
        this.numStations = numStations;
        this.simulationDuration = simulationDuration;
        this.customerArrivalRate = customerArrivalRate;
        this.stations = new ArrayList<>();
        this.queues = new ArrayList<>();
        this.queue = new Queue<>();
        this.random = new Random();
        this.totalCustomersServed = 0;
        this.totalWaitTime = 0;
        this.maxQueueLength = 0;

        for (int i = 0; i < numStations; i++) {
            stations.add(new CheckoutStation());
            queues.add(new Queue<>());
        }
    }

    public void runModelOneLine() {
        for (int currentTime = 0; currentTime < simulationDuration; currentTime++) {
            if (currentTime % customerArrivalRate == 0) {
                Customer newCustomer = new Customer(currentTime, random.nextInt(31) + 10);
                queue.enqueue(newCustomer);
                if (queue.size() > maxQueueLength) {
                    maxQueueLength = queue.size();
                }
            }

            for (CheckoutStation station : stations) {
                station.update(currentTime);

                if (station.isFree() && !queue.isEmpty()) {
                    Customer nextCustomer = queue.dequeue();
                    station.startCheckout(nextCustomer, currentTime);
                    totalWaitTime += currentTime - nextCustomer.arrivalTime;
                    totalCustomersServed++;
                }
            }
        }

        printStatistics("Model 1: One line for customers, " + numStations + " stations");
    }

    public void runModelLeastCustomers() {
        for (int currentTime = 0; currentTime < simulationDuration; currentTime++) {
            if (currentTime % customerArrivalRate == 0) {
                Customer newCustomer = new Customer(currentTime, random.nextInt(31) + 10);
                Queue<Customer> shortestLine = queues.stream().min(Comparator.comparingInt(Queue::size)).orElse(null);
                if (shortestLine != null) {
                    shortestLine.enqueue(newCustomer);
                    if (shortestLine.size() > maxQueueLength) {
                        maxQueueLength = shortestLine.size();
                    }
                }
            }

            for (int i = 0; i < numStations; i++) {
                CheckoutStation station = stations.get(i);
                station.update(currentTime);

                if (station.isFree() && !queues.get(i).isEmpty()) {
                    Customer nextCustomer = queues.get(i).dequeue();
                    station.startCheckout(nextCustomer, currentTime);
                    totalWaitTime += currentTime - nextCustomer.arrivalTime;
                    totalCustomersServed++;
                }
            }
        }

        printStatistics("Model 2: Least customers per line, " + numStations + " stations");
    }

    public void runModelRandomLine() {
        for (int currentTime = 0; currentTime < simulationDuration; currentTime++) {
            if (currentTime % customerArrivalRate == 0) {
                Customer newCustomer = new Customer(currentTime, random.nextInt(31) + 10);
                Queue<Customer> randomLine = queues.get(random.nextInt(numStations));
                randomLine.enqueue(newCustomer);
                if (randomLine.size() > maxQueueLength) {
                    maxQueueLength = randomLine.size();
                }
            }

            for (int i = 0; i < numStations; i++) {
                CheckoutStation station = stations.get(i);
                station.update(currentTime);

                if (station.isFree() && !queues.get(i).isEmpty()) {
                    Customer nextCustomer = queues.get(i).dequeue();
                    station.startCheckout(nextCustomer, currentTime);
                    totalWaitTime += currentTime - nextCustomer.arrivalTime;
                    totalCustomersServed++;
                }
            }
        }

        printStatistics("Model 3: Random line for customers, " + numStations + " stations");
    }

    private void printStatistics(String modelName) {
        double averageWaitTime = (double) totalWaitTime / totalCustomersServed;
        int avgMinutes = (int) averageWaitTime / 60;
        int avgSeconds = (int) averageWaitTime % 60;

        int elapsedMinutes = simulationDuration / 60;
        int elapsedSeconds = simulationDuration % 60;

        System.out.println(modelName);
        System.out.println("Total elapsed time: " + elapsedMinutes + " min " + elapsedSeconds + " sec");
        System.out.println("Customers served: " + totalCustomersServed);
        for (int i = 0; i < queues.size(); i++) {
            System.out.println("Length of line " + (i + 1) + ": " + queues.get(i).size());
        }
        System.out.println("Maximum queue length: " + maxQueueLength);
        System.out.println("Average customer waiting time: " + avgMinutes + " min " + avgSeconds + " sec");
        System.out.println();
    }
}

public class Checkout {
    public static void main(String[] args) {
        System.out.println("Running CheckoutOneLine:");
        runCheckoutOneLine();
        System.out.println();

        System.out.println("Running CheckoutLeastCustomers:");
        runCheckoutLeastCustomers();
        System.out.println();

        System.out.println("Running CheckoutRandomLine:");
        runCheckoutRandomLine();
    }

    public static void runCheckoutOneLine() {
        int numStations = 5;
        int simulationDuration = 7200; // 2 hours in seconds
        int customerArrivalRate = 30; // One customer every 30 seconds

        Simulation sim = new Simulation(numStations, simulationDuration, customerArrivalRate);
        sim.runModelOneLine();
    }

    public static void runCheckoutLeastCustomers() {
        int numStations = 5;
        int simulationDuration = 7200; // 2 hours in seconds
        int customerArrivalRate = 30; // One customer every 30 seconds

        Simulation sim = new Simulation(numStations, simulationDuration, customerArrivalRate);
        sim.runModelLeastCustomers();
    }

    public static void runCheckoutRandomLine() {
        int numStations = 5;
        int simulationDuration = 7200; // 2 hours in seconds
        int customerArrivalRate = 30; // One customer every 30 seconds

        Simulation sim = new Simulation(numStations, simulationDuration, customerArrivalRate);
        sim.runModelRandomLine();
    }
}
