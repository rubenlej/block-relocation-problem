package code;

import java.util.*;

public class Node implements Comparable{
    private int[][] bay;
    private int[] topContainerIndices;
    private ArrayList<Move> moves;
    private double cost;
    private PriorityQueue<Integer> containerPriorities;
    public int solutionsGenerated;
    public int maxPriority;
    public double expectedTimeCost;

    public Node(int[][] bay, double cost, PriorityQueue<Integer> containerPriorities) {
        maxPriority = -1;
        this.bay = new int[bay.length][bay[0].length];
        // Calculate lowest priority of initial layout
        for (int i = 0; i < this.bay.length; i++) {
            for (int j = 0; j < this.bay[0].length; j++) {
                this.bay[i][j] = bay[i][j];
                this.maxPriority = (bay[i][j] > maxPriority)? bay[i][j] : maxPriority;
            }
        }
        this.moves = new ArrayList<>();
        this.cost = cost;
        this.containerPriorities = containerPriorities;
        this.expectedTimeCost = 0;
        calculateTopContainerIndices();
    }

    public Node(Node node) {
        maxPriority = -1;
        this.bay = new int[node.getBay().length][node.getBay()[0].length];
        for (int i = 0; i < this.bay.length; i++) {
            for (int j = 0; j < this.bay[0].length; j++) {
                this.bay[i][j] = node.getBay()[i][j];
                this.maxPriority = (bay[i][j] > maxPriority)? bay[i][j] : maxPriority;
            }
        }
        this.moves = new ArrayList<>();
        for(int i = 0; i < node.getMoves().size(); i++) {
            Move move = node.getMoves().get(i);
            this.moves.add(move);
        }
        this.cost = node.getCost();
        this.containerPriorities = new PriorityQueue<>(node.getContainerPriorities());
        this.expectedTimeCost = node.getExpectedValue();
        calculateTopContainerIndices();
    }

    /**
     * Return the highest priority in a stack
     * @param stackIndex: index of the stack
     * @return highest priority
     */
    public int getHighestPriorityInStack(int stackIndex) {
        int highestPriority = Integer.MAX_VALUE;
        for (int i = 0; i < this.bay[0].length; i++) {
            if(this.bay[stackIndex][i] >= 0) {
                if(highestPriority > this.bay[stackIndex][i]) {
                    highestPriority = this.bay[stackIndex][i];
                }
            }
            else {
                break;
            }
        }
        return highestPriority;
    }

    /**
     * Update the array with indices of the top containers
     */
    public void calculateTopContainerIndices(){
        topContainerIndices = new int[bay.length];
        for (int i = 0; i < topContainerIndices.length; i++) {
            topContainerIndices[i] = -1;
        }
        for (int i = 0; i < bay.length; i++) {
            for (int j = 0; j < bay[0].length; j++) {
                if(bay[i][j] != -1) {
                    topContainerIndices[i] = j;
                }
            }
        }
    }

    /**
     * Check if container priotiy is in top containers
     * @param container: the container priority
     * @return the container location if container is found in the top containers, else return
     */
    public int[] isInTopContainers(int container) {
        for(int i = 0; i < topContainerIndices.length; i++) {
            if(topContainerIndices[i] != -1) {
                if(bay[i][topContainerIndices[i]] == container) {
                    return new int[]{i, topContainerIndices[i]};
                }
            }
        }
        return new int[]{-1, -1};
    }

    public int[][] getBay() {
        return bay;
    }

    public double getExpectedValue() {
        return expectedTimeCost;
    }

    public void setExpectedTimeCost(double expectedTimeCost) {
        this.expectedTimeCost = expectedTimeCost;
    }

    public ArrayList<Move> getMoves() {
        return moves;
    }

    public double getCost() {
        return cost;
    }

    public void setMoves(ArrayList<Move> moves) {
        this.moves = moves;
    }

    public PriorityQueue<Integer> getContainerPriorities() {
        return containerPriorities;
    }

    public int[] getTopContainerIndices() {
        return topContainerIndices;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getMaxPriority() {
        return maxPriority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Arrays.equals(bay, node.bay);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bay);
    }

    @Override
    public int compareTo(Object o) {
        if (this.expectedTimeCost < ((Node)o).getExpectedValue()) {
            return -1;
        }
        else if (this.expectedTimeCost > ((Node)o).getExpectedValue()) {
            return 1;
        }
        else {
            return 0;
        }
    }
}
