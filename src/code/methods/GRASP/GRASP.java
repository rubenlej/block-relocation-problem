package code.methods.GRASP;

import code.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import org.apache.commons.math3.random.MersenneTwister;

public class GRASP {
    private Problem problem;
    private Container container;
    private Gantry gantry;
    private Node initialNode;
    public static MersenneTwister randomGenerator;
    private double greedyRate;
    private int survivors;
    private double[][][] costMatrix;
    private double averageMoveCost;
    private double greedyChangeValue; // This is the fixed value beta in the paper
    private int maxDepth;
    private double totalRelocationCost;
    private int relocations;
    private int decisionIndex;
    private int trolleyStartPosition;
    public static boolean increase;

    /***
     * Initialize the GRASP input
     */
    public GRASP(Problem problem, Container container, Gantry gantry) {
        this.problem = problem;
        this.container = container;
        this.gantry = gantry;
        this.initialNode = new Node(problem.getBay(), 0, problem.getContainerPriorities());
        this.initialNode.calculateTopContainerIndices();
        this.trolleyStartPosition = 0;
    }

    /***
     * Starts the GRASP algorithm
     * @return Node of the best solution found for the problem
     */
    public Node start(double timeLimit, double greedyRate, double greedyChangeValue, int maxDepth, int survivors, int decisionIndex, boolean improvementPhase, int seed) {
        this.randomGenerator = new MersenneTwister(seed);
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long startTime = bean.getCurrentThreadCpuTime();

        int numberOfLocationsInBay = (problem.getBay().length + 1) * (problem.getBay()[0].length + 1);
        this.costMatrix = new double[problem.getBay().length + 1][numberOfLocationsInBay][numberOfLocationsInBay];
        this.averageMoveCost = GRASP.calculateCostMatrix(problem, costMatrix, gantry, container);
        this.maxDepth = maxDepth;
        this.survivors = survivors;
        this.decisionIndex = decisionIndex;
        this.greedyRate = greedyRate;
        this.greedyChangeValue = greedyChangeValue;

        double previousSolutionCost = Integer.MAX_VALUE;

        Node bestSolution = null;
        int solutionsGenerated = 0;
        relocations = 0;
        totalRelocationCost = 0;
        this.increase = true;
        while(bean.getCurrentThreadCpuTime() - startTime < timeLimit) {
            Node solution = constructSolution(new Node(initialNode), startTime, timeLimit, bean);
            if(solution != null) {
                solutionsGenerated++;
                if(improvementPhase) {
                    solution = improveByLocalSearch(solution);
                }
                if(bestSolution != null) {
                    if(solution.getCost() < bestSolution.getCost()) {
                        bestSolution = new Node(solution);
                    }
                }
                else {
                    bestSolution = new Node(solution);
                }

                if(greedyChangeValue != 0.0f) {
                    this.greedyRate = GRASP.updateGreedyRate(survivors, solution.getCost(), previousSolutionCost, this.greedyRate, greedyChangeValue);
                }
                previousSolutionCost = solution.getCost();
            }
            else {
                if(greedyChangeValue != 0.0f) {
                    // previousSolutionCost - 1 in case previousSolutionCost is also Double.MAX_VALUE, else no change will happen
                    this.greedyRate = GRASP.updateGreedyRate(survivors, Double.MAX_VALUE, previousSolutionCost - 1, this.greedyRate, greedyChangeValue);
                }
            }
        }
        if(bestSolution != null) {
            bestSolution.solutionsGenerated = solutionsGenerated;
        }
        return bestSolution;
    }


    /***
     * Generate a solution (This is the construction phase)
     * @return The generated solution node
     */
    private Node constructSolution(Node node, long startTime, double timeLimit, ThreadMXBean bean) {
        int lowestPriorityContainer = node.getMaxPriority();
        while(node.getContainerPriorities().size() > 0) {
            if (timeLimit < bean.getCurrentThreadCpuTime() - startTime) {
                return null;
            }
            if (!retrieve(costMatrix, node)) {
                if (!relocate(node, lowestPriorityContainer, decisionIndex, startTime, timeLimit, bean)) {
                    return null;
                }
                else {
                    relocations++;
                    totalRelocationCost += node.getMoves().get(node.getMoves().size() -1).getCost();
                }
            }
        }
        return node;
    }

    /***
     * Try to retrieve the container with highest priority
     * @return boolean whether a container was retrieved or not
     */
    public static boolean retrieve(double [][][] costMatrix, Node node) {
        int nextPriorityContainer = node.getContainerPriorities().peek();
        int[] retrievableStackIndex = node.isInTopContainers(nextPriorityContainer);
        int startStack = (node.getMoves().size() > 0)? node.getMoves().get(node.getMoves().size() - 1).getToStack() : 0;
        if (retrievableStackIndex[0] != -1) {
            GRASP.move(costMatrix, node, startStack, retrievableStackIndex[0], node.getBay().length, node.getTopContainerIndices()[retrievableStackIndex[0]], node.getBay()[0].length);
            node.getContainerPriorities().poll();
            return true;
        }
        return false;
    }

    /***
     * Try to relocate a blocking container
     * @return boolean whether a relocation was performed
     */
    private boolean relocate(Node node, int lowestPriorityContainer, int decisionIndex, long startTime, double timeLimit, ThreadMXBean bean) {
        // Switch to InitialGRASP if survivors is set to 0
        if(survivors == 0) {
            // For the old grasp setting a time limit is normally not necessary
            return InitialGRASP.relocate(randomGenerator, lowestPriorityContainer, greedyRate, costMatrix, node, decisionIndex, gantry, container, problem);
        }
        else {
            // In this GRASP a time limit is necessary if the depth/survivor number is high
            return proposedGRASP.relocate(randomGenerator, greedyRate, costMatrix, node, maxDepth, survivors, averageMoveCost, relocations, totalRelocationCost, startTime, timeLimit, bean);
        }
    }

    /***
     * Select the stack from which a container will be moved from
     * @return the stack index to take a container from
     */
    public static int selectFromstack(Node node, int containerPriority) {
        ArrayList<Integer> possibleFromStacks = new ArrayList<>();
        int[][] bay = node.getBay();
        for(int i = 0; i < bay.length; i++) {
            for(int j = 0; j < bay[0].length; j++) {
                if(bay[i][j] == containerPriority) {
                    possibleFromStacks.add(i);
                }
            }
        }
        return possibleFromStacks.get(randomGenerator.nextInt(possibleFromStacks.size()));
    }

    public static double updateGreedyRate(int survivors, double newCost, double oldCost, double greedyRate, double greedyChangeValue) {
        if(survivors == 0) {
            return InitialGRASP.updateGreedyRate(newCost, oldCost, greedyRate, greedyChangeValue);
        }
        else {
            return proposedGRASP.updateGreedyRate(newCost, oldCost, greedyRate, greedyChangeValue);
        }
    }

    /***
     * Perform a move on the node
     */
    public static void move(double[][][] costMatrix, Node node, int startStack, int fromStack, int toStack, int fromTier, int toTier) {
        int fromBay = 0;
        int toBay = 0;
        double moveCost = getMoveCost(costMatrix, node, startStack, fromStack, toStack, fromTier, toTier);

        ArrayList<Move> moves = node.getMoves();
        int containerPriority = node.getBay()[fromStack][fromTier];
        if(toStack != node.getBay().length) {
            node.getBay()[toStack][toTier] = node.getBay()[fromStack][fromTier];
        }
        node.getBay()[fromStack][fromTier] = -1;
        Move newMove = new Move(fromBay, toBay, fromStack, toStack, fromTier, toTier, containerPriority, moveCost);
        moves.add(newMove);
        double newCost = node.getCost() + moveCost;
        node.calculateTopContainerIndices();
        node.setCost(newCost);
    }

    /**
     * Return the cost of a move
     * @return cost of the move
     */
    public static double getMoveCost(double[][][] costMatrix, Node node, int startStack, int fromStack, int toStack, int fromTier, int toTier) {
        double moveCost = costMatrix[startStack][fromTier * (node.getBay().length + 1) + fromStack][toTier * (node.getBay().length + 1) + toStack];
        return moveCost;
    }

    /***
     * Calculate the cost matrix for the given problem
     * @return the average value of all move costs
     */
    public static double calculateCostMatrix(Problem problem, double[][][] costMatrix, Gantry gantry, Container container) {
        double totalCost = 0;

        for(int i = 0; i < costMatrix.length; i++) {
            int startStack = i;
            for (int j = 0; j < costMatrix[0].length; j++) {
                for (int k = 0; k < costMatrix[0][0].length; k++) {
                    int fromTier = j / (problem.getBay().length + 1);
                    int fromStack = j % (problem.getBay().length + 1);
                    int toTier = k / (problem.getBay().length + 1);
                    int toStack = k % (problem.getBay().length + 1);
                    //double moveCost = Move.calculateRealMoveCost(initialNode, 0,0, startStack, fromStack, toStack, fromTier, toTier, realGantry, container);
                    double moveCost = calculateMoveCost(problem, startStack, fromStack, toStack, fromTier, toTier, gantry, container);
                    totalCost += moveCost;
                    costMatrix[i][j][k] = moveCost;
                }
            }
        }
        return  totalCost / (costMatrix.length * costMatrix[0].length * costMatrix[0][0].length);
    }

    /***
     * @return the cost of a single move
     */
    public static double calculateMoveCost(Problem problem, int startStack, int fromStack, int toStack, int fromTier, int toTier, Gantry gantry, Container container) {
        double dToSourceStack = Math.abs(startStack - fromStack) * container.getWidth() / gantry.getTrolleyVelocityUnloaded();
        double dPulleySourceUnloaded = (problem.getBay()[0].length - fromTier) * container.getHeight() / gantry.getPulleyVelocityUnloaded();
        double dPulleySourceLoaded = (problem.getBay()[0].length - fromTier) * container.getHeight() / gantry.getPulleyVelocityLoaded();
        double dToTargetStack = Math.abs(fromStack - toStack) * container.getWidth() / gantry.getTrolleyVelocityLoaded();
        double dPulleyTargetLoaded = (problem.getBay()[0].length - toTier) * container.getHeight() / gantry.getPulleyVelocityLoaded();
        double dPulleyTargetUnloaded = (problem.getBay()[0].length - toTier) * container.getHeight() / gantry.getPulleyVelocityUnloaded();
        double moveCost = dToSourceStack + dPulleySourceUnloaded + dPulleySourceLoaded + dToTargetStack + dPulleyTargetLoaded + dPulleyTargetUnloaded;
        return moveCost;
    }

    /***
     * Try to improve a solution by local search
     * @return the potentially improved node
     */
    private Node improveByLocalSearch(Node solution) {
        Node newNode = new Node(solution);

        ArrayList<Move> moves = newNode.getMoves();
        ArrayList<Integer> visitedStacks = new ArrayList<>();
        // Go over all the moves, starting from the back
        for (int i = moves.size() - 1; i >= 0; i--) {
            Move move = moves.get(i);

            int toStack = move.getToStack();
            if(toStack != newNode.getBay().length) {
                newNode.getBay()[move.getFromStack()][move.getFromTier()] = move.getContainerPriority();
                newNode.getBay()[move.getToStack()][move.getToTier()] = -1;

                newNode.calculateTopContainerIndices();
                newNode = searchNewMove(newNode, move, visitedStacks, new ArrayList<>(newNode.getMoves().subList(i + 1, moves.size())));
                visitedStacks.add(newNode.getMoves().get(i).getToStack());
            }
            else {
                newNode.getBay()[move.getFromStack()][move.getFromTier()] = move.getContainerPriority();
                newNode.calculateTopContainerIndices();
            }
        }

        if(newNode.getCost() < solution.getCost()) {
            return newNode;
        }
        return solution;
    }

    /***
     * Searches for a new relocation to replace the move passed to it
     * @return the new node
     */
    private Node searchNewMove(Node node, Move move, ArrayList<Integer> visitedStacks, ArrayList<Move> movesAfterRelocation) {
        if(visitedStacks.contains(move.getToStack())) {
            return node;
        }
        Node newNode = new Node(node);

        // calculate old cost from last relocation until finish
        double totalOldCost = move.getCost();
        for(Move m : movesAfterRelocation) {
            totalOldCost += m.getCost();
        }

        int i = move.getFromStack();
        // Go over each possible position for relocation
        for (int j = 0; j < newNode.getTopContainerIndices().length; j++) {
            // if the fromStack doesn't equal the toStack and height limit has not been reached
            if (i != j && node.getTopContainerIndices()[j] + 1 < newNode.getBay()[j].length) {
                // If the old move is not repeated and the toStack hasn't been visited
                if (j != move.getToStack() && !visitedStacks.contains(j)){
                    int containerPriority = newNode.getBay()[i][newNode.getTopContainerIndices()[i]];
                    // If the move produces no blocking
                    if(!checkBlocking(newNode, j, containerPriority)){
                        ArrayList<Move> newMovesAfterRelocation = new ArrayList<>();
                        for(Move m : movesAfterRelocation) {
                            newMovesAfterRelocation.add(new Move(m));
                        }

                        int startStack = (newNode.getMoves().size() > (movesAfterRelocation.size() + 1))? newNode.getMoves().get(newNode.getMoves().size() - (movesAfterRelocation.size() + 2)).getToStack() : 0;
                        double newMoveCost = getMoveCost(costMatrix, newNode, startStack, i, j, newNode.getTopContainerIndices()[i], newNode.getTopContainerIndices()[j] + 1);

                        Move newMove = new Move(0, 0, i, j, newNode.getTopContainerIndices()[i], newNode.getTopContainerIndices()[j] + 1, containerPriority, newMoveCost);

                        // Look for the first following move that would handle the relocated container, set its fromStack to j
                        for(int k = 0; k < newMovesAfterRelocation.size(); k++) {
                            if (newMovesAfterRelocation.get(k).getContainerPriority() == move.getContainerPriority()) {
                                newMovesAfterRelocation.get(k).setFromStack(newMove.getToStack());
                                newMovesAfterRelocation.get(k).setFromTier(newMove.getToTier());
                                startStack = (k > 0)? newMovesAfterRelocation.get(k - 1).getToStack() : j;
                                double relocatedCost = getMoveCost(costMatrix, newNode, startStack, newMovesAfterRelocation.get(k).getFromStack(), newMovesAfterRelocation.get(k).getToStack(), newMovesAfterRelocation.get(k).getFromTier(), newMovesAfterRelocation.get(k).getToTier());
                                Move relocatedMove = newMovesAfterRelocation.get(k);
                                relocatedMove.setCost(relocatedCost);
                                break;
                            }
                        }

                        // Change cost of first move after relocation (startStack changed to j)
                        Move firstMoveAfterRelocation = newMovesAfterRelocation.get(0);
                        double newCostAfterRelocation = getMoveCost(costMatrix, newNode, j, newMovesAfterRelocation.get(0).getFromStack(), newMovesAfterRelocation.get(0).getToStack(), newMovesAfterRelocation.get(0).getFromTier(), newMovesAfterRelocation.get(0).getToTier());
                        firstMoveAfterRelocation.setCost(newCostAfterRelocation);

                        double totalNewCost = newMoveCost;
                        for(Move m : newMovesAfterRelocation) {
                            // Change the cost value and add to totalNewCost
                            totalNewCost += m.getCost();
                        }
                        if(totalNewCost < totalOldCost) {
                            ArrayList<Move> movesBeforeRelocation = new ArrayList<>(newNode.getMoves().subList(0, newNode.getMoves().size() - (newMovesAfterRelocation.size()+1)));
                            double costMovesBeforeRelocation = 0;
                            for( Move m : movesBeforeRelocation) {
                                costMovesBeforeRelocation += m.getCost();
                            }

                            ArrayList<Move> newMoves = new ArrayList<>();
                            newNode.getMoves().clear();
                            newMoves.addAll(movesBeforeRelocation);

                            newMoves.add(newMove);
                            newMoves.addAll(newMovesAfterRelocation);
                            newNode.setMoves(newMoves);
                            newNode.calculateTopContainerIndices();
                            newNode.setCost(costMovesBeforeRelocation + totalNewCost);
                            totalOldCost = totalNewCost;
                            movesAfterRelocation = newMovesAfterRelocation;

                        }
                    }
                }
            }
        }
        if(newNode.getCost() < node.getCost()) {
            return newNode;
        }
        return node;
    }

    /***
     * @return boolean whether the container will block a container of higher priority when placed on the toStack
     */
    public static boolean checkBlocking(Node node, int toStack, int containerPriority) {
        for(int i = 0 ; i < node.getBay()[toStack].length; i++) {
            if(node.getBay()[toStack][i] != -1) {
                if(node.getBay()[toStack][i] < containerPriority) {
                    return true;
                }
            }
            else {
                return false;
            }
        }
        return false;
    }
}
