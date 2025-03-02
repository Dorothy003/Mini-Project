import java.util.*;

public class Graph2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Graph graph = new Graph();

        System.out.print("Enter the number of edges: ");
        int edges = sc.nextInt();

        System.out.println("Enter the edges in the format: start end weight of edge :");
        for (int i = 0; i < edges; i++) {
            int u = sc.nextInt();
            int v = sc.nextInt();
            int weight = sc.nextInt();
            graph.addEdge(u, v, weight);
        }

        System.out.print("Enter the starting node: ");
        int startNode = sc.nextInt();

        System.out.print("Enter the target node: ");
        int targetNode = sc.nextInt();

        Map<Integer, Integer> prev = Dijkstra.shortestPath(graph, startNode);
        List<Integer> path = Dijkstra.reconstructPath(prev, targetNode);

        System.out.println("Shortest path from " + startNode + " to " + targetNode + ": " + path);
    }
}

class Dijkstra {
    public static Map<Integer, Integer> shortestPath(Graph graph, int start) {
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>();

        for (int node : graph.getNodes()) {
            dist.put(node, Integer.MAX_VALUE);
            prev.put(node, null);
        }

        dist.put(start, 0);
        pq.add(new Node(start, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            int u = current.id;

            for (Node neighbor : graph.getNeighbors(u)) {
                int v = neighbor.id;
                int weight = neighbor.cost;
                int newDist = dist.get(u) + weight;

                if (newDist < dist.get(v)) {
                    dist.put(v, newDist);
                    prev.put(v, u);
                    pq.add(new Node(v, newDist));
                }
            }
        }

        return prev;
    }

    public static List<Integer> reconstructPath(Map<Integer, Integer> prev, int target) {
        List<Integer> path = new ArrayList<>();
        for (Integer at = target; at != null; at = prev.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }
}

class Graph {
    private final Map<Integer, List<Node>> adjList = new HashMap<>();

    public void addEdge(int u, int v, int weight) {
        adjList.putIfAbsent(u, new ArrayList<>());
        adjList.putIfAbsent(v, new ArrayList<>());
        adjList.get(u).add(new Node(v, weight));
        adjList.get(v).add(new Node(u, weight));  // Undirected graph
    }

    public List<Node> getNeighbors(int node) {
        return adjList.getOrDefault(node, new ArrayList<>());
    }

    public Set<Integer> getNodes() {
        return adjList.keySet();
    }
}

class Node implements Comparable<Node> {
    int id;
    int cost;

    public Node(int id, int cost) {
        this.id = id;
        this.cost = cost;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.cost, other.cost);
    }
}
