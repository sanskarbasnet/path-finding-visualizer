package gui;

import javax.swing.*;
import java.awt.*;

public class PathfindingVisualizer extends JFrame {
    private final GridPanel gridPanel;
    private final JComboBox<String> algorithmDropdown;
    private final JButton startButton;
    private final JButton generateMazeButton;
    private final JRadioButton slowButton;
    private final JRadioButton mediumButton;
    private final JRadioButton fastButton;
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
        ButtonGroup speedButtonGroup = new ButtonGroup();

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
        JButton clearButton = createStyledButton("Clear Grid", new Color(231, 76, 60), new Color(192, 57, 43));
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
