import java.util.*;

public class HashFundementals {

    // Registered usernames for O(1) lookup
    private final Set<String> registeredUsernames = new HashSet<>();

    // Attempt frequency map
    private final Map<String, Integer> attemptFrequency = new HashMap<>();

    public HashFundementals() {
        // Pre-fill some usernames for demo
        registeredUsernames.addAll(Arrays.asList("john_doe", "admin", "user123"));
    }

    /** Check username availability */
    public boolean checkAvailability(String username) {
        attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);
        return !registeredUsernames.contains(username);
    }

    /** Suggest alternatives if taken */
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        int counter = 1;

        while (suggestions.size() < 5) {
            String suggestion = username + counter;
            if (!registeredUsernames.contains(suggestion)) suggestions.add(suggestion);
            counter++;
        }

        String dotSuggestion = username.replace("_", ".");
        if (!registeredUsernames.contains(dotSuggestion)) suggestions.add(dotSuggestion);

        return suggestions;
    }

    /** Register a new username */
    public boolean registerUsername(String username) {
        if (registeredUsernames.contains(username)) return false;
        registeredUsernames.add(username);
        return true;
    }

    /** Get most attempted username */
    public String getMostAttempted() {
        return attemptFrequency.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /** Demo main */
    public static void main(String[] args) {
        HashFundementals checker = new HashFundementals();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nEnter username to check (or 'exit' to quit):");
            String username = scanner.nextLine();
            if (username.equalsIgnoreCase("exit")) break;

            if (checker.checkAvailability(username)) {
                System.out.println(username + " is available! You can register.");
            } else {
                System.out.println(username + " is already taken.");
                System.out.println("Suggestions: " + checker.suggestAlternatives(username));
            }

            System.out.println("Most attempted username so far: " + checker.getMostAttempted());
        }

        scanner.close();
    }
}