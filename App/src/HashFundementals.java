import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleInventoryManager {

    // Product stock: productId -> Atomic stock count for thread-safe operations
    private final Map<String, AtomicInteger> stockMap = new ConcurrentHashMap<>();

    // Waiting list: productId -> LinkedHashMap of userId -> timestamp (FIFO order)
    private final Map<String, LinkedHashMap<Integer, Long>> waitingListMap = new ConcurrentHashMap<>();

    public FlashSaleInventoryManager() {
        // Initialize products with stock
        stockMap.put("IPHONE15_256GB", new AtomicInteger(100));
        stockMap.put("PS5_PRO", new AtomicInteger(50));
    }

    /** Check stock for a product */
    public int checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);
        return stock != null ? stock.get() : 0;
    }

    /** Attempt to purchase a product */
    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockMap.get(productId);
        if (stock == null) return "Product not found";

        // Atomically decrement stock
        int remainingStock;
        synchronized (stock) { // ensures no overselling
            remainingStock = stock.get();
            if (remainingStock > 0) {
                stock.decrementAndGet();
                return "Success, " + (remainingStock - 1) + " units remaining";
            }
        }

        // If stock is zero, add to waiting list
        waitingListMap.putIfAbsent(productId, new LinkedHashMap<>());
        LinkedHashMap<Integer, Long> waitingList = waitingListMap.get(productId);
        synchronized (waitingList) {
            if (!waitingList.containsKey(userId)) {
                waitingList.put(userId, System.currentTimeMillis());
            }
            int position = new ArrayList<>(waitingList.keySet()).indexOf(userId) + 1;
            return "Added to waiting list, position #" + position;
        }
    }

    /** Display current waiting list for a product */
    public List<Integer> getWaitingList(String productId) {
        LinkedHashMap<Integer, Long> waitingList = waitingListMap.get(productId);
        if (waitingList == null) return Collections.emptyList();
        return new ArrayList<>(waitingList.keySet());
    }

    /** Demo main */
    public static void main(String[] args) {
        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nEnter command: checkStock <product>, purchase <product> <userId>, exit");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) break;

            String[] parts = input.split(" ");
            if (parts[0].equalsIgnoreCase("checkStock") && parts.length == 2) {
                String product = parts[1];
                System.out.println(product + " → " + manager.checkStock(product) + " units available");
            } else if (parts[0].equalsIgnoreCase("purchase") && parts.length == 3) {
                String product = parts[1];
                int userId = Integer.parseInt(parts[2]);
                System.out.println(manager.purchaseItem(product, userId));
            } else {
                System.out.println("Invalid command");
            }
        }

        scanner.close();
    }
}