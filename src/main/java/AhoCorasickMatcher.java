import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;

public final class AhoCorasickMatcher implements Matcher {

    private final State mStartState;

    private AhoCorasickMatcher(Builder builder) {
        mStartState = buildAutomaton(builder.mPatternSet);
    }

    @Override
    public List<String> match(String text) {
        List<String> matches = Lists.newArrayList();
        State visiting = mStartState;

        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (visiting.getNext(c) != null) {
                visiting = visiting.getNext(c);
            } else {
                visiting = visiting.getFail();
            }

            matches.addAll(visiting.getEmits());
        }

        return matches;
    }

    @Override
    public String toString() {
        Queue<State> queue = Lists.newLinkedList();
        queue.add(mStartState);

        StringBuilder builder = new StringBuilder();
        String prefix = "{";

        while (!queue.isEmpty()) {
            builder.append(prefix);
            String innerPrefix = "";

            int statesAtThisLevel = queue.size();
            for (int i = 0; i < statesAtThisLevel; ++i) {
                State visiting = queue.remove();

                for (Map.Entry<Character, State> entry : visiting.getNextStates().entrySet()) {
                    State nextState = entry.getValue();
                    char c = entry.getKey();

                    queue.add(nextState);

                    builder.append(innerPrefix).append(c);
                    innerPrefix = ", ";
                }
            }

            prefix = "} -> {";
        }
        builder.append("}");

        return builder.toString();
    }

    private static State buildAutomaton(Set<String> patternSet) {
        final State startState = new State();

        // 1: Build TRIE
        for (String pattern : patternSet) {
            addPattern(startState, pattern, 0);
        }

        // 2: Augment TRIE with fail paths
        Queue<State> queue = Lists.newLinkedList();
        startState.setFail(startState);

        for (Map.Entry<Character, State> entry : startState.getNextStates().entrySet()) {
            State nextState = entry.getValue();
            nextState.setFail(startState);

            queue.add(nextState);
        }

        while (!queue.isEmpty()) {
            State visiting = queue.remove();

            for (Map.Entry<Character, State> entry : visiting.getNextStates().entrySet()) {
                State nextState = entry.getValue();
                char c = entry.getKey();
                State failState = visiting.getFail();

                while (failState.getNext(c) == null && !failState.equals(startState)) {
                    failState = failState.getFail();
                }

                if (failState.getNext(c) != null) {
                    failState = failState.getNext(c);
                }

                nextState.setFail(failState);
                queue.add(nextState);
            }
        }

        return startState;
    }

    private static void addPattern(final State startState, final String pattern, final int patternStart) {
        if (patternStart >= pattern.length()) {
            startState.addEmit(pattern);
            return;
        }

        char c = pattern.charAt(patternStart);
        if (startState.getNext(c) == null) {
            startState.setNext(c, new State());
        }
        addPattern(startState.getNext(c), pattern, patternStart + 1);
    }

    private static final class State {

        private final Set<String> mEmits = Sets.newHashSet();
        private final Map<Character, State> mNextStateMap = Maps.newHashMap();
        private State mFailState = null;

        public Map<Character, State> getNextStates() {
            return mNextStateMap;
        }

        public Set<String> getEmits() {
            return mEmits;
        }

        public boolean addEmit(String emit) {
            return mEmits.add(emit);
        }

        public State getNext(char c) {
            return mNextStateMap.get(c);
        }

        public State setNext(char c, State nextState) {
            return mNextStateMap.put(c, nextState);
        }

        public State getFail() {
            return mFailState;
        }

        public void setFail(State failState) {
            mFailState = failState;
        }
    }

    public static final class Builder {

        private final Set<String> mPatternSet = Sets.newHashSet();

        public Builder addPatterns(String... patterns) {
            addPatterns(Arrays.asList(patterns));
            return this;
        }

        public Builder addPatterns(Iterable<String> patterns) {
            patterns.forEach(mPatternSet::add);
            return this;
        }

        public Matcher build() {
            return new AhoCorasickMatcher(this);
        }
    }
}
