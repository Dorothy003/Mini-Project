import java.util.*;

public class Graph3 {
    private int V; // Number of vertices
    private LinkedList<Integer>[] adjList;

    public Graph3(int v) {
        V = v;
        adjList = new LinkedList[v];
        for (int i = 0; i < v; i++) {
            adjList[i] = new LinkedList<>();
        }
    }

    // Add an edge
    public void addEdge(int v, int w) {
        adjList[v].add(w);
        adjList[w].add(v);
    }

    // BFS to find the shortest path
    public List<Integer> findShortestPath(int start, int destination) {
        boolean[] visited = new boolean[V];
        int[] parent = new int[V];
        Arrays.fill(parent, -1);
        Queue<Integer> queue = new LinkedList<>();

        visited[start] = true;
        queue.add(start);

        while (!queue.isEmpty()) {
            int current = queue.poll();

            if (current == destination) {
                return constructPath(parent, start, destination);
            }

            for (int neighbor : adjList[current]) {
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    parent[neighbor] = current;
                    queue.add(neighbor);
                }
            }
        }
        return new ArrayList<>(); // Return empty list if no path found
    }

    private List<Integer> constructPath(int[] parent, int start, int destination) {
        List<Integer> path = new ArrayList<>();
        for (int at = destination; at != -1; at = parent[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter number of vertices: ");
        int vertices = scanner.nextInt();
        Graph3 g = new Graph3(vertices);

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

