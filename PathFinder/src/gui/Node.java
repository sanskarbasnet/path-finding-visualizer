package gui;

import javax.swing.*;
import java.awt.*;

public class Node extends JPanel {
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