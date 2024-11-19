package algorithms;

import gui.Node;
import gui.GridPanel;
import javax.swing.*;
import java.util.*;
import java.util.function.Supplier;

public abstract class Algorithm {
    protected List<Node> getNeighbors(Node[][] grid, Node node) {
        List<Node> neighbors = new ArrayList<>();
        int row = node.getRow(), col = node.getCol();

        if (row > 0) neighbors.add(grid[row - 1][col]);
        if (row < grid.length - 1) neighbors.add(grid[row + 1][col]);
        if (col > 0) neighbors.add(grid[row][col - 1]);
        if (col < grid[0].length - 1) neighbors.add(grid[row][col + 1]);

        return neighbors;
    }

    protected void highlightPath(Map<Node, Node> parents, Node start, Node end) {
        Node current = end;
        while (current != start) {
            current.setPath();
            current = parents.get(current);
        }
    }

    protected void noPathFound() {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null,
                        "No path found.",
                        "Search Complete",
                        JOptionPane.INFORMATION_MESSAGE));
    }

    public abstract void findPath(Node[][] grid, Node start, Node end, GridPanel gridPanel,
                                  int delay, Supplier<Boolean> stopCondition);
}