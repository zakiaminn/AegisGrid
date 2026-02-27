package com.zakiaminahmad.aegisgrid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom A* Pathfinding implementation.
 * Modified to allow enemies to calculate "breach routes" if the player boxes them out.
 * @author Zaki
 */
public class AStar {

    public static class Node {
        public int x, y;
        public int gCost, hCost, fCost;
        public Node parent;

        public Node(int x, int y) { this.x = x; this.y = y; }
        public void calculateFCost() { fCost = gCost + hCost; }
    }

    public static List<Node> findPath(Wall[][] map, int startX, int startY, int targetX, int targetY, boolean canBreakWalls) {
        List<Node> openList = new ArrayList<>();
        List<Node> closedList = new ArrayList<>();

        Node startNode = new Node(startX, startY);
        Node targetNode = new Node(targetX, targetY);

        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.get(0);
            for (int i = 1; i < openList.size(); i++) {
                if (openList.get(i).fCost < currentNode.fCost ||
                    (openList.get(i).fCost == currentNode.fCost && openList.get(i).hCost < currentNode.hCost)) {
                    currentNode = openList.get(i);
                }
            }

            openList.remove(currentNode);
            closedList.add(currentNode);

            if (currentNode.x == targetNode.x && currentNode.y == targetNode.y) {
                return retracePath(startNode, currentNode);
            }

            for (Node neighbor : getNeighbors(currentNode, map.length, map[0].length)) {
                boolean isWall = map[neighbor.x][neighbor.y] != null;

                // Block path if we hit a wall and aren't in "breaching" mode
                if (isNodeInList(closedList, neighbor) || (!canBreakWalls && isWall)) continue;

                // Add a heavy penalty for going through walls so open paths are preferred
                int wallPenalty = isWall ? 50 : 0;
                int tentativeGCost = currentNode.gCost + getDistance(currentNode, neighbor) + wallPenalty;

                if (tentativeGCost < neighbor.gCost || !isNodeInList(openList, neighbor)) {
                    neighbor.gCost = tentativeGCost;
                    neighbor.hCost = getDistance(neighbor, targetNode);
                    neighbor.calculateFCost();
                    neighbor.parent = currentNode;

                    if (!isNodeInList(openList, neighbor)) openList.add(neighbor);
                }
            }
        }
        return null; // Return null if totally trapped
    }

    private static List<Node> retracePath(Node startNode, Node endNode) {
        List<Node> path = new ArrayList<>();
        Node currentNode = endNode;
        while (currentNode != startNode) {
            path.add(currentNode);
            currentNode = currentNode.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private static List<Node> getNeighbors(Node node, int cols, int rows) {
        List<Node> neighbors = new ArrayList<>();
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            int checkX = node.x + dx[i];
            int checkY = node.y + dy[i];
            // Bounds check
            if (checkX >= 0 && checkX < cols && checkY >= 0 && checkY < rows) {
                neighbors.add(new Node(checkX, checkY));
            }
        }
        return neighbors;
    }

    private static boolean isNodeInList(List<Node> list, Node node) {
        for (Node n : list) if (n.x == node.x && n.y == node.y) return true;
        return false;
    }

    private static int getDistance(Node nodeA, Node nodeB) {
        // Manhattan distance
        return Math.abs(nodeA.x - nodeB.x) + Math.abs(nodeA.y - nodeB.y);
    }
}
