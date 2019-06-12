package code;

public class Container {
    private float height;
    private float length;
    private float width;

    public Container() {
        this.height = 1f;
        this.length = 1f;
        this.width = 1f;
    }

    public Container(float height, float length, float width) {
        this.height = height;
        this.length = length;
        this.width = width;
    }

    public float getHeight() {
        return height;
    }
    public float getLength() {
        return length;
    }
    public float getWidth() {
        return width;
    }

}
