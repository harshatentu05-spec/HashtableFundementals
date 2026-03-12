import java.util.*;

public class ParkingLot {

    private static class Vehicle {
        String licensePlate;
        long entryTime; // in milliseconds

        Vehicle(String licensePlate) {
            this.licensePlate = licensePlate;
            this.entryTime = System.currentTimeMillis();
        }
    }

    private static class Spot {
        Vehicle vehicle;
        boolean isDeleted = false;
    }

    private final Spot[] spots;
    private final int capacity;
    private int totalProbes = 0;
    private int totalParked = 0;

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        this.spots = new Spot[capacity];
        for (int i = 0; i < capacity; i++) spots[i] = new Spot();
    }

    /** Simple hash function for license plate */
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    /** Park a vehicle using linear probing */
    public synchronized String parkVehicle(String licensePlate) {
        int start = hash(licensePlate);
        int probes = 0;

        for (int i = 0; i < capacity; i++) {
            int spotIndex = (start + i) % capacity;
            Spot spot = spots[spotIndex];
            if (spot.vehicle == null || spot.isDeleted) {
                spot.vehicle = new Vehicle(licensePlate);
                spot.isDeleted = false;
                totalProbes += probes;
                totalParked++;
                return "Assigned spot #" + spotIndex + " (" + probes + " probe" + (probes != 1 ? "s" : "") + ")";
            }
            probes++;
        }
        return "Parking lot full!";
    }

    /** Exit a vehicle and calculate fee */
    public synchronized String exitVehicle(String licensePlate) {
        int start = hash(licensePlate);

        for (int i = 0; i < capacity; i++) {
            int spotIndex = (start + i) % capacity;
            Spot spot = spots[spotIndex];
            if (spot.vehicle != null && spot.vehicle.licensePlate.equals(licensePlate)) {
                long durationMs = System.currentTimeMillis() - spot.vehicle.entryTime;
                spot.vehicle = null;
                spot.isDeleted = true;
                totalParked--;
                double hours = durationMs / 3600000.0;
                double fee = Math.round(hours * 5 * 100.0) / 100.0; // $5 per hour
                return "Spot #" + spotIndex + " freed, Duration: " + String.format("%.2f h", hours) + ", Fee: $" + fee;
            }
        }
        return "Vehicle not found!";
    }

    /** Get parking statistics */
    public synchronized String getStatistics() {
        int occupied = 0;
        for (Spot spot : spots) {
            if (spot.vehicle != null) occupied++;
        }
        double occupancy = 100.0 * occupied / capacity;
        double avgProbes = totalParked == 0 ? 0 : (double) totalProbes / totalParked;
        return String.format("Occupancy: %.2f%%, Avg Probes: %.2f", occupancy, avgProbes);
    }

    /** Demo main */
    public static void main(String[] args) throws InterruptedException {
        ParkingLot lot = new ParkingLot(500);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nCommands: park <license>, exit <license>, stats, quit");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("quit")) break;

            String[] parts = input.split(" ");
            if (parts[0].equalsIgnoreCase("park") && parts.length == 2) {
                System.out.println(lot.parkVehicle(parts[1]));
            } else if (parts[0].equalsIgnoreCase("exit") && parts.length == 2) {
                System.out.println(lot.exitVehicle(parts[1]));
            } else if (parts[0].equalsIgnoreCase("stats")) {
                System.out.println(lot.getStatistics());
            } else {
                System.out.println("Invalid command!");
            }
        }

        scanner.close();
    }
}