package code;

import code.methods.GRASP.GRASP;

import java.io.*;

public class Main {
    @SuppressWarnings("Duplicates")
    public static void main(String[] args) {
        /* Parameters to set */
        double timeLimit = 3f; // Time limit for each execution per instance
        float greedyRate = 0.9f;
        float greedyChangeValue = 0.01f; // Set greedyChangeValue to 0.0f to disable reactivity
        boolean improvementPhase = true; // Set to false to disable improvement phase
        boolean firminoInstance = true; // Set to true if da Silva Firmino et al. (2019) instances are used, false if Caserta et al. instances are used
        //Proposed GRASP parameters, set survivors to 0 to run the initial GRASP algorithm
        int maxDepth = 7;
        int survivors = 2;
        // Initial GRASP parameters
        int decisionIndex = 5;

        Container container = new Container();
        Gantry gantry = new Gantry();

        File instance = new File(args[0]); // Read the BRP instance
        StringBuilder timeCostBuilder = new StringBuilder(); // Contains all solution time costs
        StringBuilder moveBuilder = new StringBuilder(); // Contains all solution moves

        int solutionsGenerated = 0; // Total amount of solutions generated
        int numberOfRelocations = 0; // Total amount of relocations in all solutions

        int seed[] = new int[] {141, 592, 653, 589, 793, 238, 462, 643, 383, 279, 502, 884, 197, 169, 399, 375, 105, 820, 974, 944, 592};

        for(int i = 0; i < seed.length; i++) {
            Problem problem = Problem.read(instance, firminoInstance);
            GRASP grasp = new GRASP(problem, container, gantry);
            Node solution = grasp.start(1000000000 * timeLimit, greedyRate, greedyChangeValue, maxDepth, survivors, decisionIndex, improvementPhase, seed[i]);

            if(solution != null) {
                solutionsGenerated += solution.solutionsGenerated;
                // Add the relocations of the solution to the total
                for(Move m : solution.getMoves()) {
                    if(m.getToStack() != solution.getBay().length) {
                        numberOfRelocations++;
                    }
                }

                // Add the solution time cost string builder
                timeCostBuilder.append(solution.getCost());
                timeCostBuilder.append("\n");
                // Add solution moves to the move string builder
                for (Move m : solution.getMoves()) {
                    moveBuilder.append(m.getFromStack() + "," + m.getFromTier() + "," + m.getToStack() + "," + m.getToTier() + "," + m.getContainerPriority() + ";");
                }
                moveBuilder.append("\n");
            }
        }

        // Write the solutions, the amount of generated solutions, the moves and the amount of relocations to different files
        PrintWriter writer;
        try {
            File dir = new File("Results");
            dir.mkdir();
            writer = new PrintWriter("Results\\\\solutions_" + instance.getName(), "UTF-8");
            writer.println(timeCostBuilder);
            writer.close();
            writer = new PrintWriter("Results\\\\generated_" + instance.getName(), "UTF-8");
            writer.println(solutionsGenerated);
            writer.close();
            writer = new PrintWriter("Results\\\\moves_" + instance.getName(), "UTF-8");
            writer.println(moveBuilder);
            writer.close();
            writer = new PrintWriter("Results\\\\relocations_" + instance.getName(), "UTF-8");
            writer.println(numberOfRelocations);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}