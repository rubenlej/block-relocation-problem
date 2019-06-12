package code;

public class Gantry {
    private float trolleyVelocityUnloaded;
    private float trolleyVelocityLoaded;
    private float gantryVelocity;
    private float pulleyVelocityUnloaded;
    private float pulleyVelocityLoaded;


    // Set the gantry speeds to the speeds found in the reactive 1.2*111GRASP paper
    public Gantry() {
        this.trolleyVelocityUnloaded = 1/1.2f;
        this.trolleyVelocityLoaded = 1/2.4f;
        this.gantryVelocity = 0.2f;
        this.pulleyVelocityUnloaded = 1/2.59f;
        this.pulleyVelocityLoaded = 1/5.18f;
    }

    public Gantry(float trolleyVelocityUnloaded, float trolleyVelocityLoaded, float gantryVelocity, float pulleyVelocityUnloaded, float pulleyVelocityLoaded) {
        this.trolleyVelocityUnloaded = trolleyVelocityUnloaded;
        this.trolleyVelocityLoaded = trolleyVelocityLoaded;
        this.gantryVelocity = gantryVelocity;
        this.pulleyVelocityUnloaded = pulleyVelocityUnloaded;
        this.pulleyVelocityLoaded = pulleyVelocityLoaded;
    }

    public float getTrolleyVelocityUnloaded() {
        return trolleyVelocityUnloaded;
    }

    public float getTrolleyVelocityLoaded() {
        return trolleyVelocityLoaded;
    }

    public float getPulleyVelocityUnloaded() {
        return pulleyVelocityUnloaded;
    }

    public float getPulleyVelocityLoaded() {
        return pulleyVelocityLoaded;
    }

}
