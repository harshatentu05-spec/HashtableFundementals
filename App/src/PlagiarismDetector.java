import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PlagiarismDetector {

    private final int nGramSize;
    private final Map<String, Set<String>> nGramIndex = new HashMap<>(); // n-gram -> set of document IDs

    public PlagiarismDetector(int nGramSize) {
        this.nGramSize = nGramSize;
    }

    /** Index all documents in a folder */
    public void indexDocuments(String folderPath) throws IOException {
        Files.list(Paths.get(folderPath))
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        indexDocument(path.toFile());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    /** Index a single document */
    public void indexDocument(File file) throws IOException {
        String docId = file.getName();
        List<String> words = extractWords(file);
        Set<String> ngrams = extractNGrams(words);

        for (String ngram : ngrams) {
            nGramIndex.computeIfAbsent(ngram, k -> new HashSet<>()).add(docId);
        }
    }

    /** Analyze a new document against indexed documents */
    public void analyzeDocument(File file) throws IOException {
        String docId = file.getName();
        List<String> words = extractWords(file);
        Set<String> ngrams = extractNGrams(words);

        Map<String, Integer> matchCount = new HashMap<>();

        for (String ngram : ngrams) {
            Set<String> docs = nGramIndex.get(ngram);
            if (docs != null) {
                for (String existingDoc : docs) {
                    if (!existingDoc.equals(docId)) {
                        matchCount.put(existingDoc, matchCount.getOrDefault(existingDoc, 0) + 1);
                    }
                }
            }
        }

        // Print similarity
        System.out.println("Analyzing " + docId + " → Extracted " + ngrams.size() + " n-grams");
        for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
            double similarity = entry.getValue() * 100.0 / ngrams.size();
            String status = similarity > 50 ? "(PLAGIARISM DETECTED)" : "(suspicious)";
            System.out.printf("→ Found %d matching n-grams with \"%s\" → Similarity: %.1f%% %s%n",
                    entry.getValue(), entry.getKey(), similarity, status);
        }
    }

    /** Extract words from a file */
    private List<String> extractWords(File file) throws IOException {
        String content = Files.readString(file.toPath()).toLowerCase().replaceAll("[^a-z0-9 ]", " ");
        return Arrays.asList(content.split("\\s+"));
    }

    /** Extract n-grams from a list of words */
    private Set<String> extractNGrams(List<String> words) {
        Set<String> ngrams = new HashSet<>();
        for (int i = 0; i <= words.size() - nGramSize; i++) {
            String ngram = String.join(" ", words.subList(i, i + nGramSize));
            ngrams.add(ngram);
        }
        return ngrams;
    }

    /** Demo main */
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        PlagiarismDetector detector = new PlagiarismDetector(5); // 5-grams

        System.out.println("Enter folder path of documents to index:");
        String folder = scanner.nextLine();
        detector.indexDocuments(folder);

        while (true) {
            System.out.println("\nEnter document file path to analyze (or 'exit'):");
            String filePath = scanner.nextLine();
            if (filePath.equalsIgnoreCase("exit")) break;

            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("File not found!");
                continue;
            }

            detector.analyzeDocument(file);
        }

        scanner.close();
    }
}