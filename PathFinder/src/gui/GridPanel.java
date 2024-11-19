package gui;

import algorithms.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

public class GridPanel extends JPanel {
    public final int rows, cols;
    public final Node[][] grid;
    public Node startNode, endNode;
    private boolean isDraggingStart = false;
    private boolean isDraggingEnd = false;
    private boolean isDrawing = false;
    private boolean isErasing = false;
    private final int cellSize = 25;
    private Node currentlySearching;
    public Point lastValidStartPos;
    public Point lastValidEndPos;

    public GridPanel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Node[rows][cols];

        setLayout(null);
        setBackground(new Color(200, 200, 200));
        setPreferredSize(new Dimension(cols * cellSize, rows * cellSize));
        initializeGrid();

        // Initialize start and end nodes at default positions
        startNode = grid[rows/2][cols/4];
        endNode = grid[rows/2][3*cols/4];
        startNode.setStart();
        endNode.setEnd();
        lastValidStartPos = new Point(cols/4, rows/2);
        lastValidEndPos = new Point(3*cols/4, rows/2);

        setupMouseListeners();
    }

    private void setupMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int col = e.getX() / cellSize;
                int row = e.getY() / cellSize;

                if (row >= 0 && row < rows && col >= 0 && col < cols) {
                    Node clickedNode = grid[row][col];
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (clickedNode == startNode) {
                            isDraggingStart = true;
                        } else if (clickedNode == endNode) {
                            isDraggingEnd = true;
                        } else if (clickedNode != startNode && clickedNode != endNode &&
                                !((PathfindingVisualizer)SwingUtilities.getWindowAncestor(GridPanel.this)).mazeGenerated) {
                            isDrawing = true;
                            clickedNode.setWall();
                        }
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        isErasing = true;
                        if (clickedNode != startNode && clickedNode != endNode) {
                            clickedNode.reset();
                        }
                    }
                }
            }

            public void mouseDragged(MouseEvent e) {
                int col = e.getX() / cellSize;
                int row = e.getY() / cellSize;

                if (row >= 0 && row < rows && col >= 0 && col < cols) {
                    Node draggedNode = grid[row][col];
                    if (isDraggingStart && draggedNode != endNode && !draggedNode.isWall()) {
                        startNode.reset();
                        startNode = draggedNode;
                        startNode.setStart();
                        lastValidStartPos = new Point(col, row);
                    } else if (isDraggingEnd && draggedNode != startNode && !draggedNode.isWall()) {
                        endNode.reset();
                        endNode = draggedNode;
                        endNode.setEnd();
                        lastValidEndPos = new Point(col, row);
                    } else if (isDrawing && SwingUtilities.isLeftMouseButton(e) &&
                            !((PathfindingVisualizer)SwingUtilities.getWindowAncestor(GridPanel.this)).mazeGenerated) {
                        if (draggedNode != startNode && draggedNode != endNode) {
                            draggedNode.setWall();
                        }
                    } else if (isErasing && SwingUtilities.isRightMouseButton(e)) {
                        if (draggedNode != startNode && draggedNode != endNode) {
                            draggedNode.reset();
                        }
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (isDraggingStart) {
                    if (startNode.isWall()) {
                        startNode.reset();
                        startNode = grid[lastValidStartPos.y][lastValidStartPos.x];
                        startNode.setStart();
                    }
                } else if (isDraggingEnd) {
                    if (endNode.isWall()) {
                        endNode.reset();
                        endNode = grid[lastValidEndPos.y][lastValidEndPos.x];
                        endNode.setEnd();
                    }
                }
                isDraggingStart = false;
                isDraggingEnd = false;
                isDrawing = false;
                isErasing = false;
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    private void initializeGrid() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Node node = new Node(row, col);
                node.setBounds(col * cellSize, row * cellSize, cellSize, cellSize);
                grid[row][col] = node;
                add(node);
            }
        }
    }

    public void setCurrentlySearching(Node node) {
        if (currentlySearching != null && currentlySearching != startNode &&
                currentlySearching != endNode) {
            currentlySearching.setVisited();
        }
        currentlySearching = node;
        if (node != null && node != startNode && node != endNode) {
            node.setSearching();
        }
    }

    public void generateMaze() {
        clearGrid();
        Random random = new Random();

        // Initialize all cells as walls
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j].setWall();
            }
        }

        // Recursive backtracking maze generation
        Stack<int[]> stack = new Stack<>();
        int startRow = 1;
        int startCol = 1;
        grid[startRow][startCol].reset();
        stack.push(new int[]{startRow, startCol});

        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int row = current[0];
            int col = current[1];

            List<int[]> unvisitedNeighbors = new ArrayList<>();

            int[][] directions = {{0, 2}, {2, 0}, {0, -2}, {-2, 0}};
            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];

                if (newRow > 0 && newRow < rows-1 && newCol > 0 && newCol < cols-1
                        && grid[newRow][newCol].isWall()) {
                    unvisitedNeighbors.add(new int[]{newRow, newCol});
                }
            }

            if (!unvisitedNeighbors.isEmpty()) {
                int[] next = unvisitedNeighbors.get(random.nextInt(unvisitedNeighbors.size()));
                int nextRow = next[0];
                int nextCol = next[1];

                grid[nextRow][nextCol].reset();
                grid[(row + nextRow)/2][(col + nextCol)/2].reset();

                stack.push(next);
            } else {
                stack.pop();
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Set start and end nodes after maze generation
        startNode = grid[1][1];
        startNode.setStart();
        lastValidStartPos = new Point(1, 1);
        endNode = grid[rows-2][cols-2];
        endNode.setEnd();
        lastValidEndPos = new Point(cols-2, rows-2);
    }

    public void startSearch(String algorithm, int delay, Supplier<Boolean> stopCondition) {
        if (startNode == null || endNode == null) {
            JOptionPane.showMessageDialog(this,
                    "Please set a start and end point.",
                    "Missing Points",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Clear previous paths
        for (Node[] row : grid) {
            for (Node node : row) {
                if (node.isVisited() || node.isPath()) {
                    SwingUtilities.invokeLater(() -> node.reset());
                }
            }
        }

        // Run the selected algorithm
        switch(algorithm) {
            case "Dijkstra":
                DijkstraAlgorithm.getInstance().findPath(grid, startNode, endNode, this, delay, stopCondition);
                break;
            case "A*":
                AStarAlgorithm.getInstance().findPath(grid, startNode, endNode, this, delay, stopCondition);
                break;
            case "Breadth First":
                BreadthFirstAlgorithm.getInstance().findPath(grid, startNode, endNode, this, delay, stopCondition);
                break;
            case "Depth First":
                DepthFirstAlgorithm.getInstance().findPath(grid, startNode, endNode, this, delay, stopCondition);
                break;
            case "Greedy Best First":
                GreedyBestFirstAlgorithm.getInstance().findPath(grid, startNode, endNode, this, delay, stopCondition);
                break;
        }
    }

    public void clearGrid() {
        startNode = null;
        endNode = null;
        currentlySearching = null;
        for (Node[] row : grid) {
            for (Node node : row) {
                node.reset();
            }
        }
    }
}