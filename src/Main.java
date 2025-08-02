import javafx.animation.*;
import javafx.application.*;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class Main extends Application {
    private static final int ROWS = 22, COLS = 36, CELL_SIZE = 30;
    private final Rectangle[][] grid = new Rectangle[ROWS][COLS];
    private final Label[][] labels = new Label[ROWS][COLS];
    private final int[][] weights = new int[ROWS][COLS];
    private Mode currentMode = Mode.OBSTACLE;
    private Point start, end;
    private InfoPanel infoPanel;
    private int visitedCount;
    private LineChart<Number, Number> timeComplexityChart;
    private XYChart.Series<Number, Number> timeSeries;
    private List<DataPoint> dataPoints;
    private StackPane chartContainer;
    private Map<String, Double> executionTimes = new HashMap<>();


    enum Mode {
        START, END, OBSTACLE
    }

    class DataPoint {
        final long time;
        final int visitedNodes;
        DataPoint(long time, int visitedNodes) {
            this.time = time;
            this.visitedNodes = visitedNodes;
        }}
    class Point {
        final int row, col;
        final Point parent;
        Point(int row, int col, Point parent) {
            this.row = row;
            this.col = col;
            this.parent = parent;
        }}
    class PointDistance {
        final int row, col, distance;
        final PointDistance parent;

        PointDistance(int row, int col, int distance, PointDistance parent) {
            this.row = row;
            this.col = col;
            this.distance = distance;
            this.parent = parent;
        }}

    class InfoPanel {
        private VBox container;
        private Label algorithmLabel;
        private Label visitedNodesLabel;
        private Label pathLengthLabel;
        private Label timeElapsedLabel;
        private Label statusLabel;
        private long startTime;

        public InfoPanel() {
            algorithmLabel = createInfoLabel("Algorithm: Not selected");
            visitedNodesLabel = createInfoLabel("Visited nodes: 0");
            pathLengthLabel = createInfoLabel("Path length: 0");
            timeElapsedLabel = createInfoLabel("Time elapsed: 0ms");
            statusLabel = createInfoLabel("Status: Ready");

            container = new VBox(10);
            container.setPadding(new Insets(15));
            container.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1;");
            container.setMinWidth(250);
            container.getStyleClass().add("info-panel");

            Label title = new Label("Algorithm Information");
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
            container.getChildren().add(title);
            container.getChildren().addAll(algorithmLabel, visitedNodesLabel, pathLengthLabel, timeElapsedLabel, statusLabel);
        }

        private Label createInfoLabel(String text) {
            Label label = new Label(text);
            label.setStyle("-fx-font-size: 14;");
            return label;
        }

        public VBox getContainer() {
            return container;
        }

        public void startTimer() {
            startTime = System.currentTimeMillis();
        }

        public void update(int visitedCount, int pathLength) {
            long elapsed = System.currentTimeMillis() - startTime;
            Platform.runLater(() -> {
                visitedNodesLabel.setText("Visited nodes: " + visitedCount);
                pathLengthLabel.setText("Path length: " + pathLength);
                timeElapsedLabel.setText("Time elapsed: " + elapsed + "ms");
            });}

        public void setAlgorithm(String algorithm) {
            Platform.runLater(() -> algorithmLabel.setText("Algorithm: " + algorithm));
        }

        public void setStatus(String status) {
            Platform.runLater(() -> statusLabel.setText("Status: " + status));
        }}

    @Override
    public void start(Stage primaryStage) {
        infoPanel = new InfoPanel();
        dataPoints = new ArrayList<>();

        // Create time complexity chart
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time (ms)");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Visited Nodes");

        timeComplexityChart = new LineChart<>(xAxis, yAxis);
        timeComplexityChart.setTitle("Time Complexity");
        timeComplexityChart.setPrefSize(300, 250);
        timeComplexityChart.setCreateSymbols(false);
        timeComplexityChart.setAnimated(false);

        timeSeries = new XYChart.Series<>();
        timeSeries.setName("Time vs Visited Nodes");
        timeComplexityChart.getData().add(timeSeries);

        chartContainer = new StackPane();
        chartContainer.setPrefHeight(300);
        VBox chartBox = new VBox(10);
        chartBox.setPadding(new Insets(15));
        chartContainer.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 1;");
        Label chartTitle = new Label("Time Complexity Analysis");
        chartTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        chartBox.getChildren().addAll(chartTitle, timeComplexityChart);
        chartContainer.getChildren().add(chartBox);
        chartContainer.getStyleClass().add("chart");

        ComboBox<String> algorithmSelector = new ComboBox<>();
        algorithmSelector.getItems().addAll("DFS", "BFS", "Dijkstra");
        algorithmSelector.setValue("Select Algorithm");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.getStyleClass().add("grid-pane");
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
                                }});
                            break;
                    }});

                grid[i][j] = cell;
                labels[i][j] = label;
                gridPane.add(cellContainer, j, i);
            }}

        Button startBtn = new Button("Set Start");
        Button endBtn = new Button("Set End");
        Button obstacleBtn = new Button("Place Weights");
        Button runBtn = new Button("Run Algorithm");
        Button comparebtn = new Button("Compare");
        Button resetbtn = new Button("Reset");

        startBtn.getStyleClass().add("button");
        endBtn.getStyleClass().add("button");
        obstacleBtn.getStyleClass().add("button");
        runBtn.getStyleClass().add("button");
        resetbtn.getStyleClass().add("button");
        comparebtn.getStyleClass().add("button");

        startBtn.setOnAction(e -> currentMode = Mode.START);
        endBtn.setOnAction(e -> currentMode = Mode.END);
        obstacleBtn.setOnAction(e -> currentMode = Mode.OBSTACLE);
        runBtn.setOnAction(e -> {
            if (start != null && end != null) {
                clearGridForNewRun();
                clearTimeComplexityChart();
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
                }}});
        comparebtn.setOnAction(e -> showBarChart());
        resetbtn.setOnAction(e -> resetGrid());

        HBox controls = new HBox(10, startBtn, endBtn, obstacleBtn, algorithmSelector, runBtn, resetbtn);
        controls.setPadding(new Insets(10));
        controls.getStyleClass().add("control-bar");
        controls.getChildren().add(comparebtn);
        // Create right panel with chart at top and info panel at bottom
        VBox rightPanel = new VBox();
        rightPanel.setSpacing(10);
        rightPanel.setPadding(new Insets(10));
        rightPanel.getChildren().addAll(chartContainer, infoPanel.getContainer());

        // Set VBox to grow info panel to fill remaining space
        VBox.setVgrow(infoPanel.getContainer(), Priority.ALWAYS);

        HBox mainContent = new HBox(10, gridPane, rightPanel);
        mainContent.setPadding(new Insets(10));
        mainContent.getStyleClass().add("main-content");

        // Set HBox to grow gridPane to fill remaining space
        HBox.setHgrow(gridPane, Priority.ALWAYS);

        VBox root = new VBox(10, controls, mainContent);
        root.setPadding(new Insets(10));

        // Set VBox to grow mainContent to fill remaining space
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Pathfinding Visualizer (DFS, BFS, Dijkstra with Animation)");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }
    private void clearTimeComplexityChart() {
        dataPoints.clear();
        timeSeries.getData().clear();
    }
    private void recordDataPoint(int visitedCount) {
        long currentTime = System.currentTimeMillis() - infoPanel.startTime;
        dataPoints.add(new DataPoint(currentTime, visitedCount));
        Platform.runLater(() -> {
            timeSeries.getData().clear();
            for (DataPoint point : dataPoints) {
                timeSeries.getData().add(new XYChart.Data<>(point.time, point.visitedNodes));
            }});}
    public void showBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Algorithm");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Execution Time (ms)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Time Complexity Comparison");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Execution Time");

        for (Map.Entry<String, Double> entry : executionTimes.entrySet()) {
            String algorithm = entry.getKey();
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());

            // Assign style class by algorithm
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    switch (algorithm) {
                        case "DFS": newNode.getStyleClass().add("bar-dfs"); break;
                        case "BFS": newNode.getStyleClass().add("bar-bfs"); break;
                        case "Dijkstra": newNode.getStyleClass().add("bar-dijkstra"); break;
                    }
                }
            });

            series.getData().add(data);
        }

        barChart.getData().add(series);
        barChart.setLegendVisible(false);
        chartContainer.getChildren().clear();
        chartContainer.getChildren().add(barChart);
    }



    private void runBFSWithAnimation() {
        infoPanel.setAlgorithm("BFS");
        infoPanel.setStatus("Running...");
        infoPanel.startTimer();
        recordDataPoint(0);
        visitedCount = 0;
        boolean[][] visited = new boolean[ROWS][COLS];
        Queue<Point> queue = new LinkedList<>();
        List<Point> animationSteps = new ArrayList<>();
        queue.add(new Point(start.row, start.col, null));
        visited[start.row][start.col] = true;
        Point endPoint = null;
        long lastRecordedTime = System.currentTimeMillis();  // Track last record time
        while (!queue.isEmpty()) {
            Point current = queue.poll();
            animationSteps.add(current);
            visitedCount++;
            //slowing
            try{
                Thread.sleep(1);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRecordedTime >= 10 || queue.isEmpty()) {  // Record every 10ms or at the end
                recordDataPoint(visitedCount);
                lastRecordedTime = currentTime;
            }
            if (current.row == end.row && current.col == end.col) {
                endPoint = current;
                break;
            }
            for (int[] dir : new int[][]{{0,1},{1,0},{0,-1},{-1,0}}) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                if (isValid(newRow, newCol) && !visited[newRow][newCol]) {
                    String text = labels[newRow][newCol].getText();
                    boolean isWeighted = !text.isEmpty();
                    if(!isWeighted) {
                        visited[newRow][newCol] = true;
                        queue.add(new Point(newRow, newCol, current));
                    }}}}
        animateSteps(animationSteps, endPoint);
        infoPanel.setStatus(endPoint != null ? "Path found!" : "No path found");
        infoPanel.update(visitedCount, calculatePathLength(endPoint));
        long elapsedTime = System.currentTimeMillis() - infoPanel.startTime;
        executionTimes.put("BFS", (double) elapsedTime);
        recordDataPoint(visitedCount);
    }
    private void runDFSWithAnimation() {
        infoPanel.setAlgorithm("DFS");
        infoPanel.setStatus("Running...");
        infoPanel.startTimer();
        recordDataPoint(0);
        visitedCount = 0;
        boolean[][] visited = new boolean[ROWS][COLS];
        Stack<Point> stack = new Stack<>();
        List<Point> animationSteps = new ArrayList<>();
        stack.push(new Point(start.row, start.col, null));
        visited[start.row][start.col] = true;
        Point endPoint = null;
        int lastRecordedCount = 0;
        while (!stack.isEmpty()) {
            Point current = stack.pop();
            animationSteps.add(current);
            visitedCount++;
            if (visitedCount - lastRecordedCount >= 5 || stack.isEmpty()) {
                recordDataPoint(visitedCount);
                lastRecordedCount = visitedCount;
            }
            if (current.row == end.row && current.col == end.col) {
                endPoint = current;
                break;
            }
            for (int[] dir : new int[][]{{0,1},{1,0},{0,-1},{-1,0}}) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                if (isValid(newRow, newCol) && !visited[newRow][newCol]) {
                    Node node = grid[newRow][newCol];
                    String text = labels[newRow][newCol].getText();
                    boolean isWeighted = !text.isEmpty();
                    if (!isWeighted) {
                        visited[newRow][newCol] = true;
                        stack.push(new Point(newRow, newCol, current));
                    }}}}
        animateSteps(animationSteps, endPoint);
        infoPanel.setStatus(endPoint != null ? "Path found!" : "No path found");
        infoPanel.update(visitedCount, calculatePathLength(endPoint));
        long elapsedTime = System.currentTimeMillis() - infoPanel.startTime;
        executionTimes.put("DFS", (double)elapsedTime);
        recordDataPoint(visitedCount);
    }
    private void runDijkstraWithAnimation() {
        infoPanel.setAlgorithm("Dijkstra");
        infoPanel.setStatus("Running...");
        infoPanel.startTimer();
        recordDataPoint(0);
        visitedCount = 0;
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
        int lastRecordedCount = 0;
        while (!pq.isEmpty()) {
            PointDistance current = pq.poll();
            if (visited[current.row][current.col]) continue;
            visited[current.row][current.col] = true;
            animationSteps.add(current);
            visitedCount++;
            if (visitedCount - lastRecordedCount >= 5 || pq.isEmpty()) {
                recordDataPoint(visitedCount);
                lastRecordedCount = visitedCount;
            }
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
                    }}}}
        animateStepsPointDistance(animationSteps, endPoint);
        infoPanel.setStatus(endPoint != null ? "Path found!" : "No path found");
        infoPanel.update(visitedCount, calculatePathLengthPointDistance(endPoint));

        long elapsedTime = System.currentTimeMillis() - infoPanel.startTime;
        executionTimes.put("Dijkstra",(double) elapsedTime);
        recordDataPoint(visitedCount);
    }
    private void animateSteps(List<Point> steps, Point endPoint) {
        Timeline timeline = new Timeline();
        int delay = 20;
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
    }
    private void drawPath(Point endPoint) {
        Point current = endPoint.parent;
        while (current != null && !(current.row == start.row && current.col == start.col)) {
            Rectangle cell = grid[current.row][current.col];
            cell.setFill(Color.YELLOW);
            current = current.parent;
        }}
    private void drawPathPointDistance(PointDistance endPoint) {
        PointDistance current = endPoint.parent;
        while (current != null && !(current.row == start.row && current.col == start.col)) {
            Rectangle cell = grid[current.row][current.col];
            cell.setFill(Color.YELLOW);
            current = current.parent;
        }}
    private int calculatePathLength(Point endPoint) {
        if (endPoint == null) return 0;
        int length = 0;
        Point current = endPoint;
        while (current != null && !(current.row == start.row && current.col == start.col)) {
            length++;
            current = current.parent;
        }
        return length;
    }
    private int calculatePathLengthPointDistance(PointDistance endPoint) {
        if (endPoint == null) return 0;
        int length = 0;
        PointDistance current = endPoint;
        while (current != null && !(current.row == start.row && current.col == start.col)) {
            length++;
            current = current.parent;
        }
        return length;
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
                    grid[finalI][finalJ].setOpacity(1.0);
                });

                ft.play();
            }}
        start = null;
        end = null;
        infoPanel.setAlgorithm("Not selected");
        infoPanel.setStatus("Ready");
        infoPanel.update(0, 0);
        clearTimeComplexityChart();
    }
    private void clearGridForNewRun() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Rectangle cell = grid[i][j];
                if (cell.getFill().equals(Color.LIGHTBLUE) || cell.getFill().equals(Color.YELLOW)) {
                    cell.setFill(Color.WHITE);
                }}}}
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static void main(String[] args) {
        launch(args);
    }}