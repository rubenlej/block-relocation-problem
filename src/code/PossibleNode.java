package code;

/***
 * Possible node class, to be expanded in a real node if node gets selected
 */
public class PossibleNode implements Comparable{
    double expectedValue;
    int toStack;

    public PossibleNode(double expectedValue, int toStack) {
        this.expectedValue = expectedValue;
        this.toStack = toStack;
    }

    public double getExpectedValue() {
        return expectedValue;
    }

    public int getToStack() {
        return toStack;
    }

    @Override
    public int compareTo(Object o) {
        if (this.expectedValue < ((PossibleNode)o).getExpectedValue()) {
            return -1;
        }
        else if (this.expectedValue > ((PossibleNode)o).getExpectedValue()) {
            return 1;
        }
        else {
            return 0;
        }
    }
}
