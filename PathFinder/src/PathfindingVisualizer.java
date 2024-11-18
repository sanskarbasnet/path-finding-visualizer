import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;

public class PathfindingVisualizer extends JFrame {
    private GridPanel gridPanel;
    private JComboBox<String> algorithmDropdown;
    private JButton startButton, clearButton, generateMazeButton;
    private ButtonGroup speedButtonGroup;
    private JRadioButton slowButton, mediumButton, fastButton;
    private static final int SLOW_DELAY = 75;
    private static final int MEDIUM_DELAY = 15;
    private static final int FAST_DELAY = 2;
    private volatile boolean searchStopped = false;
    public boolean mazeGenerated = false;

    public PathfindingVisualizer() {
        setTitle("Pathfinding Visualizer");
        setSize(1550, 820);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 245, 245));
        setResizable(false);


        // Create the grid with padding and shadow effect
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0, 0, 0, 20), 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                )
        ));
        gridWrapper.setBackground(Color.WHITE);
        gridPanel = new GridPanel(21, 59);

        // Add a help panel with modern styling
        JPanel helpPanel = new JPanel();
        helpPanel.setBackground(Color.WHITE);
        helpPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel helpText = new JLabel("<html>" +
                "<div style='font-family: Segoe UI, Arial; padding: 8px; background: #F8F9FA; border-radius: 4px;'>" +
                "<span style='color: #2ECC71; font-size: 16px'>●</span> Start Node (Draggable) &nbsp;|&nbsp; " +
                "<span style='color: #E74C3C; font-size: 16px'>●</span> End Node (Draggable) &nbsp;|&nbsp; " +
                "<span style='color: #34495E'>Wall (Left Click)</span> &nbsp;|&nbsp; " +
                "<span style='color: #7F8C8D'>Empty (Right Click to Erase)</span>" +
                "</div></html>");
        helpText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        helpPanel.add(helpText);

        gridWrapper.add(helpPanel, BorderLayout.NORTH);
        gridWrapper.add(gridPanel, BorderLayout.CENTER);
        add(gridWrapper, BorderLayout.CENTER);

        // Create modern control panel with gradient background
        JPanel controlPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(245, 245, 245),
                        0, getHeight(), new Color(235, 235, 235));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        // Style the dropdown with modern look
        algorithmDropdown = new JComboBox<>(new String[]{"Dijkstra", "A*", "Breadth First", "Depth First", "Greedy Best First"});
        algorithmDropdown.setPreferredSize(new Dimension(160, 35));
        algorithmDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        algorithmDropdown.setBackground(Color.WHITE);
        algorithmDropdown.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        algorithmDropdown.setToolTipText("Select pathfinding algorithm");

        // Create speed radio buttons panel with fixed size
        JPanel speedPanel = new JPanel();
        speedPanel.setPreferredSize(new Dimension(250, 35));
        speedPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        speedPanel.setOpaque(false);
        speedButtonGroup = new ButtonGroup();

        slowButton = new JRadioButton("Slow");
        mediumButton = new JRadioButton("Medium");
        fastButton = new JRadioButton("Fast");

        // Style radio buttons
        Font radioFont = new Font("Segoe UI", Font.PLAIN, 14);
        slowButton.setFont(radioFont);
        mediumButton.setFont(radioFont);
        fastButton.setFont(radioFont);

        slowButton.setOpaque(false);
        mediumButton.setOpaque(false);
        fastButton.setOpaque(false);

        speedButtonGroup.add(slowButton);
        speedButtonGroup.add(mediumButton);
        speedButtonGroup.add(fastButton);

        mediumButton.setSelected(true);

        speedPanel.add(slowButton);
        speedPanel.add(mediumButton);
        speedPanel.add(fastButton);

        // Style the buttons with modern design
        startButton = createStyledButton("Start Search", new Color(46, 204, 113), new Color(39, 174, 96));
        clearButton = createStyledButton("Clear Grid", new Color(231, 76, 60), new Color(192, 57, 43));
        generateMazeButton = createStyledButton("Generate Maze", new Color(52, 152, 219), new Color(41, 128, 185));

        // Add components to control panel
        controlPanel.add(algorithmDropdown);
        controlPanel.add(speedPanel);
        controlPanel.add(startButton);
        controlPanel.add(clearButton);
        controlPanel.add(generateMazeButton);

        add(controlPanel, BorderLayout.SOUTH);

        // Action listeners remain the same
        startButton.addActionListener(e -> {
            searchStopped = false;
            startButton.setEnabled(false);
            new Thread(() -> {
                int delay = getDelayFromRadioButtons();
                gridPanel.startSearch((String) algorithmDropdown.getSelectedItem(), delay, () -> searchStopped);
                SwingUtilities.invokeLater(() -> startButton.setEnabled(true));
            }).start();
        });

        clearButton.addActionListener(e -> {
            searchStopped = true;
            mazeGenerated = false;
            gridPanel.clearGrid();
            gridPanel.startNode = gridPanel.grid[gridPanel.rows/2][gridPanel.cols/4];
            gridPanel.endNode = gridPanel.grid[gridPanel.rows/2][3*gridPanel.cols/4];
            gridPanel.startNode.setStart();
            gridPanel.endNode.setEnd();
            gridPanel.lastValidStartPos = new Point(gridPanel.cols/4, gridPanel.rows/2);
            gridPanel.lastValidEndPos = new Point(3*gridPanel.cols/4, gridPanel.rows/2);
        });

        generateMazeButton.addActionListener(e -> {
            generateMazeButton.setEnabled(false);
            new Thread(() -> {
                mazeGenerated = true;
                gridPanel.generateMaze();
                SwingUtilities.invokeLater(() -> generateMazeButton.setEnabled(true));
            }).start();
        });
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setBackground(new Color(245, 245, 245));
        label.setOpaque(true);
        return label;
    }

    private int getDelayFromRadioButtons() {
        if (slowButton.isSelected()) return SLOW_DELAY;
        if (mediumButton.isSelected()) return MEDIUM_DELAY;
        if (fastButton.isSelected()) return FAST_DELAY;
        return MEDIUM_DELAY;
    }

    private JButton createStyledButton(String text, Color baseColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    //g2d.setColor(hoverColor);
                } else if (getModel().isRollover()) {
                    g2d.setColor(baseColor.brighter());
                } else {
                    g2d.setColor(baseColor);
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        button.setPreferredSize(new Dimension(160, 35));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            PathfindingVisualizer visualizer = new PathfindingVisualizer();
            visualizer.setLocationRelativeTo(null);
            visualizer.setVisible(true);
        });
    }
}

class GridPanel extends JPanel {
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
                        } else if (clickedNode != startNode && clickedNode != endNode && !((PathfindingVisualizer)SwingUtilities.getWindowAncestor(GridPanel.this)).mazeGenerated) {
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
                    } else if (isDrawing && SwingUtilities.isLeftMouseButton(e) && !((PathfindingVisualizer)SwingUtilities.getWindowAncestor(GridPanel.this)).mazeGenerated) {
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
                    // If start node is on a wall, return to last valid position
                    if (startNode.isWall()) {
                        startNode.reset();
                        startNode = grid[lastValidStartPos.y][lastValidStartPos.x];
                        startNode.setStart();
                    }
                } else if (isDraggingEnd) {
                    // If end node is on a wall, return to last valid position
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
        if (currentlySearching != null && currentlySearching != startNode && currentlySearching != endNode) {
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
        lastValidEndPos = new Point(cols-3, rows-2);
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
                DijkstraAlgorithm.findPath(grid, startNode, endNode, this, delay, stopCondition);
                break;
            case "A*":
                AStarAlgorithm.findPath(grid, startNode, endNode, this, delay, stopCondition);
                break;
            case "Breadth First":
                BreadthFirstAlgorithm.findPath(grid, startNode, endNode, this, delay, stopCondition);
                break;
            case "Depth First":
                DepthFirstAlgorithm.findPath(grid, startNode, endNode, this, delay, stopCondition);
                break;
            case "Greedy Best First":
                GreedyBestFirstAlgorithm.findPath(grid, startNode, endNode, this, delay, stopCondition);
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

class Node extends JPanel {
    private final int row, col;
    private boolean isStart, isEnd, isWall, isVisited, isPath, isSearching;

    public Node(int row, int col) {
        this.row = row;
        this.col = col;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public void setStart() {
        isStart = true;
        setBackground(new Color(46, 204, 113));
    }

    public void setEnd() {
        isEnd = true;
        setBackground(new Color(231, 76, 60));
    }

    public void setWall() {
        if (!isStart && !isEnd) {
            isWall = true;
            setBackground(new Color(52, 73, 94));
        }
    }

    public void setSearching() {
        if (!isStart && !isEnd) {
            isSearching = true;
            isVisited = false;
            SwingUtilities.invokeLater(() ->
                    setBackground(new Color(255, 165, 0))); // Orange for currently searching
        }
    }

    public void setVisited() {
        if (!isStart && !isEnd) {
            isVisited = true;
            isSearching = false;
            SwingUtilities.invokeLater(() ->
                    setBackground(new Color(52, 152, 219)));
        }
    }

    public void setPath() {
        if (!isStart && !isEnd) {
            isPath = true;
            SwingUtilities.invokeLater(() ->
                    setBackground(new Color(241, 196, 15)));
        }
    }

    public void reset() {
        isStart = isEnd = isWall = isVisited = isPath = isSearching = false;
        setBackground(Color.WHITE);
    }

    public boolean isWall() { return isWall; }
    public boolean isVisited() { return isVisited; }
    public boolean isPath() { return isPath; }
    public boolean isStart() { return isStart; }
    public boolean isEnd() { return isEnd; }
}

class DijkstraAlgorithm {
    public static void findPath(Node[][] grid, Node start, Node end, GridPanel gridPanel, int delay, Supplier<Boolean> stopCondition) {
        PriorityQueue<NodeWrapper> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));
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

    protected static List<Node> getNeighbors(Node[][] grid, Node node) {
        List<Node> neighbors = new ArrayList<>();
        int row = node.getRow(), col = node.getCol();

        if (row > 0) neighbors.add(grid[row - 1][col]);
        if (row < grid.length - 1) neighbors.add(grid[row + 1][col]);
        if (col > 0) neighbors.add(grid[row][col - 1]);
        if (col < grid[0].length - 1) neighbors.add(grid[row][col + 1]);

        return neighbors;
    }

    protected static void highlightPath(Map<Node, Node> parents, Node start, Node end) {
        Node current = end;
        while (current != start) {
            current.setPath();
            current = parents.get(current);
        }
    }

    protected static void noPathFound() {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null,
                        "No path found.",
                        "Search Complete",
                        JOptionPane.INFORMATION_MESSAGE));
    }

    protected static class NodeWrapper {
        Node node;
        int cost;

        NodeWrapper(Node node, int cost) {
            this.node = node;
            this.cost = cost;
        }
    }
}

class AStarAlgorithm extends DijkstraAlgorithm {
    public static void findPath(Node[][] grid, Node start, Node end, GridPanel gridPanel, int delay, Supplier<Boolean> stopCondition) {
        PriorityQueue<NodeWrapper> openSet = new PriorityQueue<>(
                Comparator.comparingInt(n -> n.cost + manhattanDistance(n.node, end)));
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

    private static int manhattanDistance(Node a, Node b) {
        return Math.abs(a.getRow() - b.getRow()) + Math.abs(a.getCol() - b.getCol());
    }
}

class BreadthFirstAlgorithm extends DijkstraAlgorithm {
    public static void findPath(Node[][] grid, Node start, Node end, GridPanel gridPanel, int delay, Supplier<Boolean> stopCondition) {
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

class DepthFirstAlgorithm extends DijkstraAlgorithm {
    public static void findPath(Node[][] grid, Node start, Node end, GridPanel gridPanel, int delay, Supplier<Boolean> stopCondition) {
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

class GreedyBestFirstAlgorithm extends DijkstraAlgorithm {
    public static void findPath(Node[][] grid, Node start, Node end, GridPanel gridPanel, int delay, Supplier<Boolean> stopCondition) {
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

    private static int manhattanDistance(Node a, Node b) {
        return Math.abs(a.getRow() - b.getRow()) + Math.abs(a.getCol() - b.getCol());
    }
}
