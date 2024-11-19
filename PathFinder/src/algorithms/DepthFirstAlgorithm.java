package algorithms;

import gui.Node;
import gui.GridPanel;
import java.util.*;
import java.util.function.Supplier;

public class DepthFirstAlgorithm extends Algorithm {
    private static DepthFirstAlgorithm instance;

    private DepthFirstAlgorithm() {}

    public static DepthFirstAlgorithm getInstance() {
        if (instance == null) {
            instance = new DepthFirstAlgorithm();
        }
        return instance;
    }

    @Override
    public void findPath(Node[][] grid, Node start, Node end, GridPanel gridPanel,
                         int delay, Supplier<Boolean> stopCondition) {
        Stack<Node> stack = new Stack<>();
        Map<Node, Node> parents = new HashMap<>();
        Set<Node> visited = new HashSet<>();

        stack.push(start);
        visited.add(start);

        while (!stack.isEmpty() && !stopCondition.get()) {
            Node current = stack.pop();

            gridPanel.setCurrentlySearching(current);

            if (current == end) {
                highlightPath(parents, start, end);
                return;
            }

            for (Node neighbor : getNeighbors(grid, current)) {
                if (!visited.contains(neighbor) && !neighbor.isWall()) {
                    visited.add(neighbor);
                    parents.put(neighbor, current);
                    stack.push(neighbor);
                }
            }

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (!stopCondition.get()) {
            noPathFound();
        }
    }
}