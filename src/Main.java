import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;


import java.util.*;

public class Main extends Application {
    private static final int ROWS = 20;
    private static final int COLS = 20;
    private static final int CELL_SIZE = 30;

    private final Rectangle[][] grid = new Rectangle[ROWS][COLS];
    private final Label[][] labels = new Label[ROWS][COLS];
    private final int[][] weights = new int[ROWS][COLS];
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

    // extra
    class PointDistance {
        int row, col, distance;
        PointDistance parent;

        PointDistance(int row, int col, int distance, PointDistance parent) {
            this.row = row;
            this.col = col;
            this.distance = distance;
            this.parent = parent;
        }
    }

    @Override
    public void start(Stage primaryStage) {

        ComboBox<String> algorithmSelector = new ComboBox<>();
        algorithmSelector.getItems().addAll("DFS", "BFS", "Dijkstra");
        algorithmSelector.setValue("Select Algorithm"); // Default selection


        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE, Color.WHITE);
                cell.setStroke(Color.GRAY);

                Label label = new Label("");
                label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                label.setTextFill(Color.BLACK);
                label.setMouseTransparent(true);

                StackPane cellContainer = new StackPane();
                cellContainer.getChildren().addAll(cell, label);

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
                            TextInputDialog dialog = new TextInputDialog("1");
                            dialog.setHeaderText("Enter Weight (1-100):");
                            Optional<String> result = dialog.showAndWait();
                            result.ifPresent(value -> {
                                try {
                                    int weight = Integer.parseInt(value);
                                    if (weight >= 1 && weight <= 100) {
                                        weights[finalI][finalJ] = weight;
                                        cell.setFill(Color.WHITE);
                                        label.setTextFill(Color.BLACK);
                                        label.setText(String.valueOf(weight));
                                    } else {
                                        showError("Please enter a weight between 1 and 100.");
                                    }
                                } catch (NumberFormatException ex) {
                                    showError("Invalid input.");
                                }
                            });
                            break;
                    }
                });

                grid[i][j] = cell;
                labels[i][j] = label;
                gridPane.add(cellContainer, j, i);
            }
        }

        //Creating buttons-
        Button startBtn = new Button("Set Start");
        Button endBtn = new Button("Set End");
        Button obstacleBtn = new Button("Place Weights");
        Button runBtn = new Button("Run Algorithm");
        Button resetbtn = new Button("Reset");
        //Components with css
        startBtn.getStyleClass().add("button");
        endBtn.getStyleClass().add("button");
        obstacleBtn.getStyleClass().add("button");
        runBtn.getStyleClass().add("button");
        resetbtn.getStyleClass().add("button");

        //Components with event listener
        startBtn.setOnAction(e -> currentMode = Mode.START);
        endBtn.setOnAction(e -> currentMode = Mode.END);
        obstacleBtn.setOnAction(e -> currentMode = Mode.OBSTACLE);
        runBtn.setOnAction(e -> {
            if (start != null && end != null) {
                clearGridForNewRun();
                String selected = algorithmSelector.getValue();
                switch (selected) {
                    case "DFS":
                        runDFSWithAnimation();
                        break;
                    case "BFS":
                        runBFSWithAnimation();
                        break;
                    case "Dijkstra":
                        runDijkstraWithAnimation();
                        break;
                }
            }
        });
        resetbtn.setOnAction(e->resetGrid());
        /*
        //Function to call BFS
        bfsBtn.setOnAction(e -> {
            if (start != null && end != null) {
                runBFSWithAnimation();
            }
        });

        dfsBtn.setOnAction(e -> {
            if (start != null && end != null) {
                runDFSWithAnimation();
            }
        });

        dijkstraBtn.setOnAction(e -> {
            if (start != null && end != null) {
                runDijkstraWithAnimation();
            }
        }); // extra*/
        HBox controls = new HBox(10, startBtn, endBtn, obstacleBtn, algorithmSelector, runBtn, resetbtn);
        controls.setPadding(new Insets(10));

        VBox root = new VBox(10, controls, gridPane);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Pathfinding Visualizer (DFS , BFS,  Dijkstra with Animation)");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
    //Breadth First Search algorithm
    private void runBFSWithAnimation() {
        boolean[][] visited = new boolean[ROWS][COLS];
        Queue<Point> queue = new LinkedList<>();
        List<Point> animationSteps = new ArrayList<>();

        queue.add(new Point(start.row, start.col, null));
        visited[start.row][start.col] = true;

        Point endPoint = null;

        while (!queue.isEmpty()) {
            Point current = queue.poll();
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
                    queue.add(new Point(newRow, newCol, current));
                }
            }
        }

        animateSteps(animationSteps, endPoint);
    }

    //Depth First Search algorithm
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
    //Dijkstra algorithm
    private void runDijkstraWithAnimation() {
        int[][] distance = new int[ROWS][COLS];
        boolean[][] visited = new boolean[ROWS][COLS];

        for (int[] row : distance) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }

        PriorityQueue<PointDistance> pq = new PriorityQueue<>(Comparator.comparingInt(p -> p.distance));
        List<PointDistance> animationSteps = new ArrayList<>();

        distance[start.row][start.col] = 0;
        pq.add(new PointDistance(start.row, start.col, 0, null));

        PointDistance endPoint = null;

        while (!pq.isEmpty()) {
            PointDistance current = pq.poll();

            if (visited[current.row][current.col]) continue;
            visited[current.row][current.col] = true;
            animationSteps.add(current);

            if (current.row == end.row && current.col == end.col) {
                endPoint = current;
                break;
            }

            for (int[] dir : new int[][]{{0,1},{1,0},{0,-1},{-1,0}}) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];

                if (isValid(newRow, newCol) && !visited[newRow][newCol]) {
                    int weight = (weights[newRow][newCol] == 0) ? 1 : weights[newRow][newCol];
                    int newDist = current.distance + weight;

                    if (newDist < distance[newRow][newCol]) {
                        distance[newRow][newCol] = newDist;
                        pq.add(new PointDistance(newRow, newCol, newDist, current));
                    }
                }
            }
        }

        animateStepsPointDistance(animationSteps, endPoint);
    } //extra

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

    private void animateStepsPointDistance(List<PointDistance> steps, PointDistance endPoint) {
        Timeline timeline = new Timeline();
        int delay = 20;

        for (int i = 0; i < steps.size(); i++) {
            PointDistance p = steps.get(i);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(i * delay), e -> {
                Rectangle cell = grid[p.row][p.col];
                if (!cell.getFill().equals(Color.LIMEGREEN) && !cell.getFill().equals(Color.RED)) {
                    cell.setFill(Color.LIGHTBLUE);
                }
            });
            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.setOnFinished(e -> {
            if (endPoint != null) drawPathPointDistance(endPoint);
        });

        timeline.play();
    } // extra

    private void drawPath(Point endPoint) {
        Point current = endPoint.parent;
        while (current != null && !(current.row == start.row && current.col == start.col)) {
            Rectangle cell = grid[current.row][current.col];
            cell.setFill(Color.YELLOW); // final path
            current = current.parent;
        }
    }

    private void drawPathPointDistance(PointDistance endPoint) {
        PointDistance current = endPoint.parent;
        while (current != null && !(current.row == start.row && current.col == start.col)) {
            Rectangle cell = grid[current.row][current.col];
            cell.setFill(Color.YELLOW);
            current = current.parent;
        }
    }


    private boolean isValid(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }
    private void resetGrid() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                FadeTransition ft = new FadeTransition(Duration.millis(300), grid[i][j]);
                ft.setFromValue(1.0);
                ft.setToValue(0.0);
                int finalI = i;
                int finalJ = j;

                ft.setOnFinished(e -> {
                    grid[finalI][finalJ].setFill(Color.WHITE);
                    weights[finalI][finalJ] = 1;
                    labels[finalI][finalJ].setText("");
                    grid[finalI][finalJ].setOpacity(1.0); // Reset opacity back to full after fade
                });

                ft.play();
            }
        }
        start = null;
        end = null;
    }

    private void clearGridForNewRun() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Rectangle cell = grid[i][j];
                if (cell.getFill().equals(Color.LIGHTBLUE) || cell.getFill().equals(Color.YELLOW)) {
                    cell.setFill(Color.WHITE);
                }
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static void main(String[] args) {
        launch(args);
    }
}






