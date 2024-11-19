package algorithms;

import gui.Node;
import gui.GridPanel;
import java.util.*;
import java.util.function.Supplier;

public class GreedyBestFirstAlgorithm extends Algorithm {
    private static GreedyBestFirstAlgorithm instance;

    private GreedyBestFirstAlgorithm() {}

    public static GreedyBestFirstAlgorithm getInstance() {
        if (instance == null) {
            instance = new GreedyBestFirstAlgorithm();
        }
        return instance;
    }

    protected static class NodeWrapper {
        Node node;
        int cost;

        NodeWrapper(Node node, int cost) {
            this.node = node;
            this.cost = cost;
        }
    }

    @Override
    public void findPath(Node[][] grid, Node start, Node end, GridPanel gridPanel,
                         int delay, Supplier<Boolean> stopCondition) {
        PriorityQueue<NodeWrapper> openSet = new PriorityQueue<>(
                Comparator.comparingInt(n -> manhattanDistance(n.node, end)));
        Map<Node, Node> parents = new HashMap<>();
        Set<Node> visited = new HashSet<>();

        openSet.add(new NodeWrapper(start, 0));
        visited.add(start);

        while (!openSet.isEmpty() && !stopCondition.get()) {
            NodeWrapper currentWrapper = openSet.poll();
            Node current = currentWrapper.node;

            gridPanel.setCurrentlySearching(current);

            if (current == end) {
                highlightPath(parents, start, end);
                return;
            }

            for (Node neighbor : getNeighbors(grid, current)) {
                if (!visited.contains(neighbor) && !neighbor.isWall()) {
                    visited.add(neighbor);
                    parents.put(neighbor, current);
                    openSet.add(new NodeWrapper(neighbor, manhattanDistance(neighbor, end)));
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

    private int manhattanDistance(Node a, Node b) {
        return Math.abs(a.getRow() - b.getRow()) + Math.abs(a.getCol() - b.getCol());
    }
}