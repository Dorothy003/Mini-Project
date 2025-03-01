import java.util.*;

public class graph1 {
    private int V; // Number of vertices
    private LinkedList<Integer>[] adjList;
    private List<Integer> shortestPath;
    private int minPathLength;

   
    public graph1(int v) {
        V = v;
        adjList = new LinkedList[v];
        for (int i = 0; i < v; i++) {
            adjList[i] = new LinkedList<>();
        }
        shortestPath = new ArrayList<>();
        minPathLength = Integer.MAX_VALUE;
    }

    // Add an edge
    public void addEdge(int v, int w) {
        adjList[v].add(w);
        adjList[w].add(v); 
    }

    // DFS to find the shortest path
    private void dfs(int current, int destination, boolean[] visited, List<Integer> path) {
        visited[current] = true;
        path.add(current);

        // If destination is reached, update the shortest path
        if (current == destination) {
            if (path.size() < minPathLength) {
                minPathLength = path.size();
                shortestPath = new ArrayList<>(path);
            }
        } else {
            for (int neighbor : adjList[current]) {
                if (!visited[neighbor]) {
                    dfs(neighbor, destination, visited, path);
                }
            }
        }

        // Backtrack
        visited[current] = false;
        path.remove(path.size() - 1);
    }

    public List<Integer> findShortestPath(int start, int destination) {
        boolean[] visited = new boolean[V];
        List<Integer> path = new ArrayList<>();
        dfs(start, destination, visited, path);
        return shortestPath;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        
        System.out.print("Enter number of vertices: ");
        int vertices = scanner.nextInt();
        graph1 g = new graph1(vertices);

        
        System.out.print("Enter number of edges: ");
        int edges = scanner.nextInt();

        System.out.println("Enter the edges (format: u v for each edge):");
        for (int i = 0; i < edges; i++) {
            int u = scanner.nextInt();
            int v = scanner.nextInt();
            g.addEdge(u, v);
        }

        
        System.out.print("Enter start node: ");
        int start = scanner.nextInt();

        System.out.print("Enter destination node: ");
        int destination = scanner.nextInt();

        List<Integer> shortestPath = g.findShortestPath(start, destination);

        
        if (!shortestPath.isEmpty()) {
            System.out.println("Shortest path from " + start + " to " + destination + ": " + shortestPath);
        } else {
            System.out.println("No path found.");
        }

        scanner.close();
    }
}

