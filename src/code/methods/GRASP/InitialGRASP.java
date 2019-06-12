package code.methods.GRASP;

import code.*;
import org.apache.commons.math3.random.MersenneTwister;

import java.util.*;

public class InitialGRASP {

    /**
     * Try to relocate a blocking container
     * @return boolean whether a relocation was performed or not
     */
    public static boolean relocate(MersenneTwister randomGenerator, int lowestPriorityContainer, double greedyRate, double[][][] costMatrix, Node node, int decisionIndex, Gantry gantry, Container container, Problem problem) {
        int nextContainer = node.getContainerPriorities().peek();
        int fromStack = GRASP.selectFromstack(node, nextContainer);

        ArrayList<int[]> rlc = createRestrictedRCL(node, lowestPriorityContainer, fromStack, node.getTopContainerIndices()[fromStack], decisionIndex, greedyRate);
        if(rlc.size() != 0) {
            int[] randomMove = rlc.get(randomGenerator.nextInt(rlc.size()));
            int startStack = (node.getMoves().size() > 0)? node.getMoves().get(node.getMoves().size() - 1).getToStack() : 0;
            GRASP.move(costMatrix, node, startStack, randomMove[0], randomMove[1], node.getTopContainerIndices()[randomMove[0]], node.getTopContainerIndices()[randomMove[1]] + 1);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Create the Restricted Candidate List (RCL)
     * @return the RCL
     */
    private static ArrayList<int[]> createRestrictedRCL(Node node, int lowestPriorityContainer, int fromStack, int fromTier, int decisionIndex, double greedyRate) {
        ArrayList<int[]> rcl = new ArrayList<>();
        float maxDecisionIndexValue = 0;
        float minDecisionIndexValue = Float.MAX_VALUE;

        for(int i = 0; i < node.getTopContainerIndices().length; i++) {
            if(!(fromTier == -1) && fromStack != i && node.getTopContainerIndices()[i] + 1 < node.getBay()[i].length) {
                float calculatedDecisionIndexValue = calculateMNI(node, lowestPriorityContainer, fromStack, i);
                minDecisionIndexValue = (minDecisionIndexValue > calculatedDecisionIndexValue) ? calculatedDecisionIndexValue : minDecisionIndexValue;
                maxDecisionIndexValue = (maxDecisionIndexValue < calculatedDecisionIndexValue) ? calculatedDecisionIndexValue : maxDecisionIndexValue;
            }
        }

        double greedyLimit = maxDecisionIndexValue - greedyRate * (maxDecisionIndexValue - minDecisionIndexValue);

        for(int i = 0; i < node.getTopContainerIndices().length; i++) {
            if(!(fromTier == -1) && fromStack != i && node.getTopContainerIndices()[i] + 1 < node.getBay()[i].length) {
                if (calculateMNI(node, lowestPriorityContainer, fromStack, i) <= greedyLimit) {
                    rcl.add(new int[]{fromStack,i});
                }
            }
        }
        return rcl;
    }

    /**
     * Calculate the Min-Max Index for a relocation
     */
    public static float calculateMNI(Node node, int lowestPriorityContainer, int fromStack, int toStack) {
        int[][] bay = node.getBay();

        int containerPriority = bay[fromStack][node.getTopContainerIndices()[fromStack]];
        int highestPriority =  node.getHighestPriorityInStack(toStack);
        if(highestPriority == Integer.MAX_VALUE) {
            highestPriority = lowestPriorityContainer + 1;
        }

        int diff = highestPriority - containerPriority;

        if(diff < 0) {
            if(node.getTopContainerIndices()[toStack] == bay[0].length - 2) {
                diff = - lowestPriorityContainer - highestPriority - containerPriority;
            }
            diff = Math.abs(diff) + 2 * lowestPriorityContainer;
        }
        return diff;
    }

    /***
     * Update the greedy rate
     * @return the new greedy rate
     */
    public static double updateGreedyRate(double newCost, double oldCost, double greedyRate, double greedyChangeValue) {
        double newGreedyRate = greedyRate;

        if(newCost >= oldCost) {
            if(GRASP.increase) {
                GRASP.increase = false;
            }
            else {
                GRASP.increase = true;
            }
        }
        if(GRASP.increase) {
            if(newGreedyRate + greedyChangeValue <= 1) {
                newGreedyRate += greedyChangeValue;
            }
        }
        else {
            if(newGreedyRate - greedyChangeValue >= 0) {
                newGreedyRate -= greedyChangeValue;
            }
        }
        return newGreedyRate;
    }
}