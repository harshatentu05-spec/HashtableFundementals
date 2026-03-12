import java.util.*;
import java.text.*;

public class TransactionAnalyzer {

    static class Transaction {
        int id;
        double amount;
        String merchant;
        long timestamp; // in milliseconds
        String accountId;

        Transaction(int id, double amount, String merchant, String time, String accountId) throws ParseException {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.timestamp = parseTimeToMillis(time);
            this.accountId = accountId;
        }

        private long parseTimeToMillis(String time) throws ParseException {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            return sdf.parse(time).getTime();
        }

        @Override
        public String toString() {
            return "Transaction{id:" + id + ", amount:" + amount + ", merchant:" + merchant + ", time:" + timestamp + "}";
        }
    }

    private final List<Transaction> transactions = new ArrayList<>();

    /** Add a transaction */
    public void addTransaction(Transaction tx) {
        transactions.add(tx);
    }

    /** Classic Two-Sum */
    public List<int[]> findTwoSum(double target) {
        Map<Double, Transaction> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();
        for (Transaction tx : transactions) {
            double complement = target - tx.amount;
            if (map.containsKey(complement)) {
                result.add(new int[]{map.get(complement).id, tx.id});
            }
            map.put(tx.amount, tx);
        }
        return result;
    }

    /** Two-Sum within a time window (ms) */
    public List<int[]> findTwoSumWithTimeWindow(double target, long windowMillis) {
        List<int[]> result = new ArrayList<>();
        transactions.sort(Comparator.comparingLong(t -> t.timestamp));

        Map<Double, Transaction> map = new HashMap<>();
        for (Transaction tx : transactions) {
            // Remove transactions outside the time window
            map.entrySet().removeIf(e -> Math.abs(tx.timestamp - e.getValue().timestamp) > windowMillis);

            double complement = target - tx.amount;
            if (map.containsKey(complement)) {
                result.add(new int[]{map.get(complement).id, tx.id});
            }
            map.put(tx.amount, tx);
        }
        return result;
    }

    /** K-Sum (recursive) */
    public List<List<Integer>> findKSum(int k, double target) {
        List<List<Integer>> result = new ArrayList<>();
        transactions.sort(Comparator.comparingDouble(t -> t.amount));
        kSumHelper(0, k, target, new ArrayList<>(), result);
        return result;
    }

    private void kSumHelper(int start, int k, double target, List<Integer> path, List<List<Integer>> result) {
        if (k == 2) {
            int left = start, right = transactions.size() - 1;
            while (left < right) {
                double sum = transactions.get(left).amount + transactions.get(right).amount;
                if (Math.abs(sum - target) < 1e-6) {
                    List<Integer> temp = new ArrayList<>(path);
                    temp.add(transactions.get(left).id);
                    temp.add(transactions.get(right).id);
                    result.add(temp);
                    left++;
                    right--;
                } else if (sum < target) left++;
                else right--;
            }
        } else {
            for (int i = start; i < transactions.size() - k + 1; i++) {
                path.add(transactions.get(i).id);
                kSumHelper(i + 1, k - 1, target - transactions.get(i).amount, path, result);
                path.remove(path.size() - 1);
            }
        }
    }

    /** Detect duplicates: same amount, same merchant, different accounts */
    public List<String> detectDuplicates() {
        Map<String, Set<String>> map = new HashMap<>();
        for (Transaction tx : transactions) {
            String key = tx.amount + "|" + tx.merchant;
            map.computeIfAbsent(key, k -> new HashSet<>()).add(tx.accountId);
        }
        List<String> duplicates = new ArrayList<>();
        for (Map.Entry<String, Set<String>> e : map.entrySet()) {
            if (e.getValue().size() > 1) duplicates.add(e.getKey() + " → accounts: " + e.getValue());
        }
        return duplicates;
    }

    /** Demo main */
    public static void main(String[] args) throws ParseException {
        TransactionAnalyzer analyzer = new TransactionAnalyzer();
        analyzer.addTransaction(new Transaction(1, 500, "Store A", "10:00", "acc1"));
        analyzer.addTransaction(new Transaction(2, 300, "Store B", "10:15", "acc2"));
        analyzer.addTransaction(new Transaction(3, 200, "Store C", "10:30", "acc3"));
        analyzer.addTransaction(new Transaction(4, 500, "Store A", "10:45", "acc2"));

        System.out.println("Two-Sum target 500:");
        for (int[] pair : analyzer.findTwoSum(500)) {
            System.out.println(Arrays.toString(pair));
        }

        System.out.println("\nTwo-Sum within 1 hour:");
        for (int[] pair : analyzer.findTwoSumWithTimeWindow(500, 3600000)) {
            System.out.println(Arrays.toString(pair));
        }

        System.out.println("\nK-Sum k=3, target=1000:");
        for (List<Integer> list : analyzer.findKSum(3, 1000)) {
            System.out.println(list);
        }

        System.out.println("\nDuplicate detection:");
        for (String s : analyzer.detectDuplicates()) {
            System.out.println(s);
        }
    }
}