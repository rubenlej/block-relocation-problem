package code.methods.GRASP;

import code.*;
import org.apache.commons.math3.random.MersenneTwister;

import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

// GRASP algorithm based on the working times
public class proposedGRASP {
    private static ArrayList<Node> candidates;
    public static ThreadMXBean bean;

    /***
     * Relocate
     * @return boolean whether a relocation was performed
     */
    public static boolean relocate(MersenneTwister randomGenerator, double greedyRate, double[][][] costMatrix, Node node, int maxDepth, int survivors, double averageMoveCost, int relocations, double totalRelocationCost, long startTime, double timeLimit, ThreadMXBean newBean) {
        candidates = new ArrayList<>();
        bean = newBean;
        int startStack = (node.getMoves().size() > 0)? node.getMoves().get(node.getMoves().size() - 1).getToStack() : 0;
        ArrayList<Node> startList = new ArrayList<>();
        startList.add(new Node(node));
        expandRCL(costMatrix, startList, maxDepth, 0, survivors, averageMoveCost, relocations, totalRelocationCost, startTime, timeLimit);
        candidates = filterCandidatesWithGreedyLimit(greedyRate);
        if(candidates.size() != 0) {
            Node randomNode = candidates.get(randomGenerator.nextInt(candidates.size()));
            Move randomMove = randomNode.getMoves().get(node.getMoves().size());
            GRASP.move(costMatrix, node, startStack, randomMove.getFromStack(), randomMove.getToStack(), randomMove.getFromTier(), randomMove.getToTier());
            return true;
        }
        return false;
    }

    /***
     * Expand the RCL to a new depth, adding all leaves on the max depth to the candidate list
     */
    private static void expandRCL(double[][][] costMatrix, ArrayList<Node> nodes, int maxDepth, int depth, int survivors, double averageMoveCost, int relocations, double totalRelocationCost, long startTime, double timeLimit) {
        if (bean.getCurrentThreadCpuTime() - startTime < timeLimit) {
            if (maxDepth > depth)  {
                for (Node node : nodes) {
                    if (bean.getCurrentThreadCpuTime() - startTime < timeLimit) {
                        if (node.getContainerPriorities().size() == 0) {
                            candidates.add(node);
                        } else {
                            if (GRASP.retrieve(costMatrix, node)) {
                                // Expand on this new node alone
                                ArrayList<Node> startList = new ArrayList<>();
                                startList.add(node);
                                expandRCL(costMatrix, startList, maxDepth, depth + 1, survivors, averageMoveCost, relocations, totalRelocationCost, startTime, timeLimit);
                            } else {
                                ArrayList<Node> rcl = createRestrictedRCLNodes(costMatrix, node, survivors, averageMoveCost, relocations, totalRelocationCost);
                                expandRCL(costMatrix, rcl, maxDepth, depth + 1, survivors, averageMoveCost, relocations, totalRelocationCost, startTime, timeLimit);
                            }
                        }
                    }
                    else {
                        candidates.clear();
                        break;
                    }
                }
            } else {
                candidates.addAll(nodes);
            }
        }
        else {
            candidates.clear();
        }
    }

    /***
     * @return create the survivor list
     */
    private static ArrayList<Node> createRestrictedRCLNodes(double[][][] costMatrix, Node node, int survivors, double averageMoveCost, int relocations, double totalRelocationCost) {
        int fromStack = GRASP.selectFromstack(node, node.getContainerPriorities().peek());
        int fromTier = node.getTopContainerIndices()[fromStack];
        ArrayList<Move> moves = node.getMoves();
        PriorityQueue<PossibleNode> possibleNodes = new PriorityQueue<>();

        int startStack;
        if (moves.size() > 0) {
            startStack = moves.get(moves.size() - 1).getToStack();
        }
        else {
            startStack = 0;
        }

        for(int i = 0; i < node.getTopContainerIndices().length; i++) {
            if(!(fromTier == -1) && fromStack != i && node.getTopContainerIndices()[i] + 1 < node.getBay()[i].length) {
                double calculatedValue = calculateExpectedValue(costMatrix, node, startStack, fromStack, i, fromTier, node.getTopContainerIndices()[i] + 1, averageMoveCost, relocations, totalRelocationCost);
                possibleNodes.add(new PossibleNode(calculatedValue, i));
            }
        }

        ArrayList<Node> survivorList = new ArrayList<>();
        for(int i = 0; i < survivors; i++) {
            if(possibleNodes.size() > 0) {
                PossibleNode possibleNode = possibleNodes.poll();
                Node newNode = new Node(node);
                GRASP.move(costMatrix, newNode, startStack, fromStack, possibleNode.getToStack(), fromTier, node.getTopContainerIndices()[possibleNode.getToStack()] + 1);
                newNode.setExpectedTimeCost(newNode.getExpectedValue() + possibleNode.getExpectedValue());
                survivorList.add(newNode);
            }
        }
        return survivorList;
    }

    /**
     * Filter the candidate list
     * @param greedyRate
     * @return filtered RCL
     */
    private static ArrayList<Node> filterCandidatesWithGreedyLimit(double greedyRate) {
        ArrayList<Node> remainingNodes = new ArrayList<>();
        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;
        for(int i = 0; i < candidates.size(); i++) {
            minValue = (minValue > candidates.get(i).getExpectedValue()) ? candidates.get(i).getExpectedValue() : minValue;
            maxValue = (maxValue < candidates.get(i).getExpectedValue()) ? candidates.get(i).getExpectedValue() : maxValue;
        }
        double greedyLimit = maxValue - greedyRate * (maxValue - minValue);
        for(int i = 0; i < candidates.size(); i++) {
            if (candidates.get(i).getExpectedValue() <= greedyLimit) {
                remainingNodes.add(candidates.get(i));
            }
        }
        return remainingNodes;
    }

    /**
     * Calculate an expected time cost for a certain relocation
     * @return the expected time cost
     */
    private static double calculateExpectedValue(double[][][] costMatrix, Node node, int startStack, int fromStack, int toStack, int fromTier, int toTier, double averageMoveCost, int relocations, double totalRelocationCost) {
        double moveCost = GRASP.getMoveCost(costMatrix, node, startStack, fromStack, toStack, fromTier, toTier);
        double blockingCost = 0;
        // prevent division by 0
        //TODO; average move cost instead
        if(relocations == 0) {
            blockingCost = GRASP.checkBlocking(node, toStack, node.getBay()[fromStack][fromTier])? blockingCost + averageMoveCost : blockingCost;
        }
        else {
            blockingCost = GRASP.checkBlocking(node, toStack, node.getBay()[fromStack][fromTier])? blockingCost + totalRelocationCost / relocations : blockingCost;
        }
        return moveCost + blockingCost;
    }

    /***
     * Update the greedy rate
     * @return the new greedy rate
     */
    public static double updateGreedyRate(double newCost, double oldCost, double greedyRate, double greedyChangeValue) {
        double newGreedyRate = greedyRate;
        if(newCost >= oldCost && newGreedyRate > 0) {
            newGreedyRate -= greedyChangeValue;
        }
        else if(newCost < oldCost && newGreedyRate < 1) {
            newGreedyRate += greedyChangeValue;
        }
        return newGreedyRate;
    }

}