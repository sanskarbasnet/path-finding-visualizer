package algorithms;

import gui.Node;
import gui.GridPanel;
import java.util.*;
import java.util.function.Supplier;

public class DijkstraAlgorithm extends Algorithm {
    private static DijkstraAlgorithm instance;

    private DijkstraAlgorithm() {}

    public static DijkstraAlgorithm getInstance() {
        if (instance == null) {
            instance = new DijkstraAlgorithm();
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
                Comparator.comparingInt(n -> n.cost));
        Map<Node, Integer> costs = new HashMap<>();
        Map<Node, Node> parents = new HashMap<>();

        openSet.add(new NodeWrapper(start, 0));
        costs.put(start, 0);

        while (!openSet.isEmpty() && !stopCondition.get()) {
            NodeWrapper currentWrapper = openSet.poll();
            Node current = currentWrapper.node;

            gridPanel.setCurrentlySearching(current);

            if (current == end) {
                highlightPath(parents, start, end);
                return;
            }

            for (Node neighbor : getNeighbors(grid, current)) {
                if (neighbor.isWall()) continue;

                int newCost = costs.get(current) + 1;
                if (newCost < costs.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    costs.put(neighbor, newCost);
                    parents.put(neighbor, current);
                    openSet.add(new NodeWrapper(neighbor, newCost));
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