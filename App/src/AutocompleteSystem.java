import java.util.*;

public class AutocompleteSystem {

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfQuery = false;
    }

    private final TrieNode root = new TrieNode();
    private final Map<String, Integer> queryFrequency = new HashMap<>();

    /** Insert a new query into the Trie and update frequency */
    public void addQuery(String query) {
        queryFrequency.put(query, queryFrequency.getOrDefault(query, 0) + 1);

        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEndOfQuery = true;
    }

    /** Get top K suggestions for a given prefix */
    public List<String> getSuggestions(String prefix, int topK) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return Collections.emptyList();
        }

        // Collect all queries under this prefix
        List<String> allQueries = new ArrayList<>();
        collectQueries(node, new StringBuilder(prefix), allQueries);

        // Sort by frequency descending, then lexicographically
        allQueries.sort((q1, q2) -> {
            int freqCompare = queryFrequency.get(q2).compareTo(queryFrequency.get(q1));
            return freqCompare != 0 ? freqCompare : q1.compareTo(q2);
        });

        return allQueries.size() > topK ? allQueries.subList(0, topK) : allQueries;
    }

    /** Recursive helper to collect queries under a Trie node */
    private void collectQueries(TrieNode node, StringBuilder prefix, List<String> result) {
        if (node.isEndOfQuery) result.add(prefix.toString());
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            prefix.append(entry.getKey());
            collectQueries(entry.getValue(), prefix, result);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    /** Demo main */
    public static void main(String[] args) {
        AutocompleteSystem system = new AutocompleteSystem();

        // Preload some queries
        system.addQuery("java tutorial");
        system.addQuery("javascript");
        system.addQuery("java download");
        system.addQuery("java tutorial");
        system.addQuery("java 21 features");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nEnter prefix to search (or 'exit'):");
            String prefix = scanner.nextLine();
            if (prefix.equalsIgnoreCase("exit")) break;

            List<String> suggestions = system.getSuggestions(prefix, 10);
            if (suggestions.isEmpty()) {
                System.out.println("No suggestions found.");
            } else {
                int rank = 1;
                for (String s : suggestions) {
                    System.out.printf("%d. \"%s\" (%d searches)%n", rank++, s, system.queryFrequency.get(s));
                }
            }

            // Optional: update frequency based on new selection
            if (!suggestions.isEmpty()) system.addQuery(suggestions.get(0));
        }

        scanner.close();
    }
}

