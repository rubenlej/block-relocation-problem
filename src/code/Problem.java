package code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;

/***
 * This class represents a Block Relocation Problem
 */
public class Problem {
    private int[][] bay;
    private PriorityQueue<Integer> containerPriorities;
    private int lowestPriority;

    public Problem(int[][] mainBay) {
        this.bay = mainBay;
        this.containerPriorities = getContainerPriorities();
        this.lowestPriority = calculateLowestPriority();
    }

    private int calculateLowestPriority() {
        int lowestPriority = 0;
        for (int i = 0; i < bay.length; i++) {
            for (int j = 0; j < bay[0].length; j++) {
                if(bay[i][j] > lowestPriority) {
                    lowestPriority = bay[i][j];
                }
            }
        }
        return lowestPriority;
    }

    public PriorityQueue<Integer> getContainerPriorities() {
        containerPriorities = new PriorityQueue<>();
        for (int i = 0; i < bay.length; i++) {
            for (int j = 0; j < bay[0].length; j++) {
                if (bay[i][j] != -1) {
                    containerPriorities.add(bay[i][j]);
                }
            }
        }
        return containerPriorities;
    }

    public static Problem read(File instance, boolean firminoInstance) {
        if (instance == null) { return null; }
        if(firminoInstance) {
            return readFirminoInstance(instance);
        }
        else {
            return readCasertaInstance(instance);
        }
    }

    public static Problem readFirminoInstance(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int maxHeight = Integer.parseInt(reader.readLine().split("Tiers: ")[1].trim());
            int stacks = Integer.parseInt(reader.readLine().split("Stacks: ")[1].trim());
            int containerNumber = Integer.parseInt(reader.readLine().split("Containers: ")[1].trim());
            int[][] bay = new int[stacks][maxHeight];

            for (int i = 0; i < stacks; i++) {
                for (int j = 0; j < maxHeight; j++) {
                    bay[i][j] = -1;
                }
            }

            for (int i = 0; i < stacks; i++) {
                // Remove leading, trailing and multiple spaces, then split on space
                String[] stackInfo = reader.readLine().split(": ");
                if(stackInfo.length > 1) {
                    stackInfo = stackInfo[1].trim().replaceAll(" +", " ").split(" ");
                    for(int j = 0; j < stackInfo.length ; j++) {
                        bay[i][j] = Integer.parseInt(stackInfo[j]);
                    }
                }
            }

            return new Problem(bay);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Problem readCasertaInstance(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // As the max height we choose
            //int maxHeight = Integer.parseInt(reader.readLine().split(" ")[0]) + 2;
            int stacks = Integer.parseInt(reader.readLine().split(" ")[0]);

            ArrayList<String[]> bayInfo = new ArrayList<>();
            for (int i = 0; i < stacks; i++) {
                // Remove leading, trailing and multiple spaces, then split on space
                String nextLine = reader.readLine();
                if(nextLine != null) {
                    String[] stackInfo = nextLine.split(" ");
                    if(stackInfo.length > 1) {
                        bayInfo.add(stackInfo);
                    }
                }
            }

            int maxHeight = 0;
            for(int i = 1; i < bayInfo.size() ; i++) {
                if(Integer.parseInt(bayInfo.get(i)[0]) > maxHeight) {
                    maxHeight = Integer.parseInt(bayInfo.get(i)[0]);
                }
            }

            // Add 2 tiers to allow for relocations
            maxHeight += 2;

            int[][] bay = new int[stacks][maxHeight];

            for (int i = 0; i < stacks; i++) {
                for (int j = 0; j < maxHeight; j++) {
                    bay[i][j] = -1;
                }
            }
            for (int i = 0; i < stacks; i++) {
                for (int j = 1; j < bayInfo.get(i).length; j++) {
                    bay[i][j - 1] = Integer.parseInt(bayInfo.get(i)[j]);
                }
            }

            Problem problem = new Problem(bay);

            return problem;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int[][] getBay() {
        return bay;
    }
}
