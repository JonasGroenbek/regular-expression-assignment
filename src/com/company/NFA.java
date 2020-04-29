package com.company;

import java.util.Stack;

public class NFA {

    private Digraph graph;     // digraph of epsilon transitions
    private String regexp;     // regular expression
    private final int m;       // number of characters in regular expression

    /**
     * Creates the non-deterministic finite automaton based on a provided regularexpression.
     * This method supports 3 operators, the or "|", capturing group "()" and zero or more "*".
     * If more quantifiers, character classes etc. Should be implemented in our regular expression
     * engine, it should be included in the NFA since it is responsible for creating the graph
     * for the underlying non-deterministic flow of the operations made for matching on the
     * provided regular expression.
     * @param regexp
     */
    public NFA(String regexp) {
        this.regexp = regexp;
        m = regexp.length();
        //this stack is the operations to be made in order to match the string against the regular expression
        Stack<Integer> ops = new Stack();
        //creates a graph with the amount of characters in the regular expression + 1 vertices
        graph = new Digraph(m+1);
        for (int i = 0; i < m; i++) {
            int lp = i;
            if (regexp.charAt(i) == '(' || regexp.charAt(i) == '|')
                ops.push(i);
            else if (regexp.charAt(i) == ')') {
                int or = ops.pop();

                // 2-way or operator
                if (regexp.charAt(or) == '|') {
                    lp = ops.pop();
                    graph.addEdge(lp, or+1);
                    graph.addEdge(or, i);
                }
                else if (regexp.charAt(or) == '(')
                    lp = or;
                else assert false;
            }

            // closure operator (uses 1-character lookahead)
            if (i < m-1 && regexp.charAt(i+1) == '*') {
                graph.addEdge(lp, i+1);
                graph.addEdge(i+1, lp);
            }
            if (regexp.charAt(i) == '(' || regexp.charAt(i) == '*' || regexp.charAt(i) == ')')
                graph.addEdge(i, i+1);
        }
        if (ops.size() != 0)
            throw new IllegalArgumentException("Invalid regular expression");
    }

    /**
     * Returns true if the text is matched by the regular expression.
     */
    public boolean recognizes(String txt) {
        DirectedDFS dfs = new DirectedDFS(graph, 0);
        Bag<Integer> pc = new Bag();
        for (int v = 0; v < graph.V(); v++)
            if (dfs.marked(v)) pc.add(v);

        // Compute possible NFA states for txt[i+1]
        for (int i = 0; i < txt.length(); i++) {
            if (txt.charAt(i) == '*' || txt.charAt(i) == '|' || txt.charAt(i) == '(' || txt.charAt(i) == ')')
                throw new IllegalArgumentException("text contains the metacharacter '" + txt.charAt(i) + "'");

            Bag<Integer> match = new Bag();
            for (int v : pc) {
                if (v == m) continue;
                if ((regexp.charAt(v) == txt.charAt(i)) || regexp.charAt(v) == '.')
                    match.add(v+1);
            }
            dfs = new DirectedDFS(graph, match);
            pc = new Bag();
            for (int v = 0; v < graph.V(); v++)
                if (dfs.marked(v)) pc.add(v);

            // optimization if no states reachable
            if (pc.size() == 0) return false;
        }

        // check for accept state
        for (int v : pc)
            if (v == m) return true;
        return false;
    }

    public static void main(String[] args) {
        if(args.length > 1) {
            System.out.println("running with command line arguments");
            String regexp = "(" + args[0] + ")";
            String txt = args[1];
            NFA nfa = new NFA(regexp);
            System.out.println(nfa.recognizes(txt));
        } else {
            System.out.println("running with predefined regular expression and test string");
            String testRegex = "(aa*b)";
            String testString = "aaaab";
            NFA nfa = new NFA(testRegex);
            System.out.println(nfa.recognizes(testString));
        }
    }

}