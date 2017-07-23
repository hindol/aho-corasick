public class App {

    public static void main(String[] args) {
        Matcher matcher = new AhoCorasickMatcher.Builder()
                .addPatterns("he", "she", "his", "hers")
                .build();
        System.out.println(matcher.match("she and her cat went to his home"));
    }
}
