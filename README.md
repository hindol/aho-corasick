# aho-corasick

Multi-pattern String Search in Java using Aho Corasick String Matching Algorithm

```java
Matcher matcher = new AhoCorasickMatcher.Builder()
        .addPatterns("he", "she", "his", "hers")
        .build();
System.out.println(matcher.match("she and her cat went to his home"));
```
