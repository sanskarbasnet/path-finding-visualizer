package algorithms;

import gui.Node;
import gui.GridPanel;
import java.util.*;
import java.util.function.Supplier;

public class BreadthFirstAlgorithm extends Algorithm {
    private static BreadthFirstAlgorithm instance;

    private BreadthFirstAlgorithm() {}

    public static BreadthFirstAlgorithm getInstance() {
        if (instance == null) {
            instance = new BreadthFirstAlgorithm();
        }
        return instance;
    }

    @Override
    public void findPath(Node[][] grid, Node start, Node end, GridPanel gridPanel,
                         int delay, Supplier<Boolean> stopCondition) {
        Queue<Node> queue = new LinkedList<>();
        Map<Node, Node> parents = new HashMap<>();
        Set<Node> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && !stopCondition.get()) {
            Node current = queue.poll();

            gridPanel.setCurrentlySearching(current);

            if (current == end) {
                highlightPath(parents, start, end);
                return;
            }

            for (Node neighbor : getNeighbors(grid, current)) {
                if (!visited.contains(neighbor) && !neighbor.isWall()) {
                    visited.add(neighbor);
                    parents.put(neighbor, current);
                    queue.add(neighbor);
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