import java.util.*;

public class UsernameChecker {

    private final Map<String, String> usernameToUserId = new HashMap<>();
    private final Map<String, Integer> attemptFrequency = new HashMap<>();

    /** Check if a username is available */
    public boolean checkAvailability(String username) {
        attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);
        return !usernameToUserId.containsKey(username);
    }

    /** Register a username */
    public boolean registerUsername(String username, String userId) {
        if (usernameToUserId.containsKey(username)) return false;
        usernameToUserId.put(username, userId);
        attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);
        return true;
    }

    /** Suggest alternative usernames */
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;
            if (!usernameToUserId.containsKey(suggestion)) suggestions.add(suggestion);
        }
        suggestions.add(username.replace("_", "."));
        return suggestions;
    }

    /** Get the most attempted username */
    public String getMostAttempted() {
        return attemptFrequency.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /** Demo main */
    public static void main(String[] args) {
        UsernameChecker checker = new UsernameChecker();

        // Preload some usernames
        checker.registerUsername("john_doe", "user1");
        checker.registerUsername("admin", "admin1");

        // Check availability
        System.out.println("john_doe available? " + checker.checkAvailability("john_doe")); // false
        System.out.println("jane_smith available? " + checker.checkAvailability("jane_smith")); // true

        // Suggest alternatives
        System.out.println("Suggestions for john_doe: " + checker.suggestAlternatives("john_doe"));

        // Simulate some attempts
        checker.checkAvailability("admin");
        checker.checkAvailability("admin");
        checker.checkAvailability("john_doe");

        // Most attempted username
        System.out.println("Most attempted: " + checker.getMostAttempted());
    }
}