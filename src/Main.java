import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class Main extends Application {
    private static final int ROWS = 20;
    private static final int COLS = 20;
    private static final int CELL_SIZE = 30;

    private final Rectangle[][] grid = new Rectangle[ROWS][COLS];
    private Mode currentMode = Mode.OBSTACLE;
    private Point start = null;
    private Point end = null;

    enum Mode {
        START, END, OBSTACLE
    }

    class Point {
        int row, col;
        Point parent;

        Point(int row, int col, Point parent) {
            this.row = row;
            this.col = col;
            this.parent = parent;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE, Color.WHITE);
                cell.setStroke(Color.GRAY);

                int finalI = i;
                int finalJ = j;

                cell.setOnMouseClicked(e -> {
                    switch (currentMode) {
                        case START:
                            if (start != null) {
                                grid[start.row][start.col].setFill(Color.WHITE);
                            }
                            start = new Point(finalI, finalJ, null);
                            cell.setFill(Color.LIMEGREEN);
                            break;
                        case END:
                            if (end != null) {
                                grid[end.row][end.col].setFill(Color.WHITE);
                            }
                            end = new Point(finalI, finalJ, null);
                            cell.setFill(Color.RED);
                            break;
                        case OBSTACLE:
                            if (cell.getFill() == Color.WHITE) {
                                cell.setFill(Color.BLACK);
                            } else if (cell.getFill() == Color.BLACK) {
                                cell.setFill(Color.WHITE);
                            }
                            break;
                    }
                });

                grid[i][j] = cell;
                gridPane.add(cell, j, i);
            }
        }

        Button startBtn = new Button("Set Start");
        Button endBtn = new Button("Set End");
        Button obstacleBtn = new Button("Place Obstacles");
        Button dfsBtn = new Button("Run DFS");

        startBtn.setOnAction(e -> currentMode = Mode.START);
        endBtn.setOnAction(e -> currentMode = Mode.END);
        obstacleBtn.setOnAction(e -> currentMode = Mode.OBSTACLE);
        dfsBtn.setOnAction(e -> {
            if (start != null && end != null) {
                runDFSWithAnimation();
            }
        });

        HBox controls = new HBox(10, startBtn, endBtn, obstacleBtn, dfsBtn);
        controls.setPadding(new Insets(10));

        VBox root = new VBox(10, controls, gridPane);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Pathfinding Visualizer (DFS with Animation)");
        primaryStage.show();
    }

    private void runDFSWithAnimation() {
        boolean[][] visited = new boolean[ROWS][COLS];
        Stack<Point> stack = new Stack<>();
        List<Point> animationSteps = new ArrayList<>();

        stack.push(new Point(start.row, start.col, null));
        visited[start.row][start.col] = true;

        Point endPoint = null;

        while (!stack.isEmpty()) {
            Point current = stack.pop();
            animationSteps.add(current);

            if (current.row == end.row && current.col == end.col) {
                endPoint = current;
                break;
            }

            for (int[] dir : new int[][]{{0,1},{1,0},{0,-1},{-1,0}}) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];

                if (isValid(newRow, newCol) && !visited[newRow][newCol] && grid[newRow][newCol].getFill() != Color.BLACK) {
                    visited[newRow][newCol] = true;
                    stack.push(new Point(newRow, newCol, current));
                }
            }
        }

        animateSteps(animationSteps, endPoint);
    }

    private void animateSteps(List<Point> steps, Point endPoint) {
        Timeline timeline = new Timeline();
        int delay = 20; // delay of 20 milliseconds   so that it looks like the grids are being explored like a snake

        for (int i = 0; i < steps.size(); i++) {
            Point p = steps.get(i);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(i * delay), e -> {
                Rectangle cell = grid[p.row][p.col];
                if (!cell.getFill().equals(Color.LIMEGREEN) && !cell.getFill().equals(Color.RED)) {
                    cell.setFill(Color.LIGHTBLUE);
                }
            });
            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.setOnFinished(e -> {
            if (endPoint != null) drawPath(endPoint);
        });

        timeline.play();
    }

    private void drawPath(Point endPoint) {
        Point current = endPoint.parent;
        while (current != null && !(current.row == start.row && current.col == start.col)) {
            Rectangle cell = grid[current.row][current.col];
            cell.setFill(Color.YELLOW); // final path
            current = current.parent;
        }
    }

    private boolean isValid(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public static void main(String[] args) {
        launch(args);
    }
}






