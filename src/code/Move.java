package code;

public class Move {
    private int fromBay;
    private int toBay;
    private int fromStack;
    private int toStack;
    private int fromTier;
    private int toTier;
    private int containerPriority;
    private double cost;

    public Move(int fromBay, int toBay, int fromStack, int toStack, int fromTier, int toTier, int containerPriority, double cost) {
        this.fromBay = fromBay;
        this.toBay = toBay;
        this.fromStack = fromStack;
        this.toStack = toStack;
        this.fromTier = fromTier;
        this.toTier = toTier;
        this.containerPriority = containerPriority;
        this.cost = cost;
    }

    public Move(Move move) {
        this.fromBay = move.getFromBay();
        this.toBay = move.getToBay();
        this.fromStack = move.getFromStack();
        this.toStack = move.getToStack();
        this.fromTier = move.getFromTier();
        this.toTier = move.getToTier();
        this.containerPriority = move.getContainerPriority();
        this.cost = move.getCost();
    }

    public int getFromBay() {
        return fromBay;
    }

    public int getToBay() {
        return toBay;
    }

    public int getFromStack() {
        return fromStack;
    }

    public int getToStack() {
        return toStack;
    }

    public int getFromTier() {
        return fromTier;
    }

    public int getToTier() {
        return toTier;
    }

    public int getContainerPriority() {
        return containerPriority;
    }

    public double getCost() {
        return cost;
    }

    public void setFromStack(int fromStack) {
        this.fromStack = fromStack;
    }

    public void setFromTier(int fromTier) {
        this.fromTier = fromTier;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}
