package brownianmotion;

import cellindexmethod.CellIndexMethod;
import models.MassParticle;
import models.Particle;

import java.util.*;

/**
 * Created by sebastian on 4/6/17.
 */
public class BrownianMotion extends CellIndexMethod {

    private Map<Integer, List<? extends Particle>> simulation = new HashMap<>();

    public BrownianMotion(double l, double rc, List<? extends Particle> particles) {
        super(l, rc, particles, false);
        simulation.put(0, particles);
    }

    public void simulate (int T) {
        for (int t = 0; t < T; t++) {
            reloadMatrix(simulation.get(t));
            calculateDistances(t);
            t++;
        }
    }

    // Private functions

    private void calculateDistances(int T) {
        Map<Particle, Set<Particle>> neighbours = findNeighbors(simulation.get(T));
        List<Particle> nextStep = new ArrayList<>();

        for (Particle p : simulation.get(T)) {
            MassParticle mp = (MassParticle) p;

            Set<Particle> particleNeighbours = neighbours.get(T);
        }
    }
}
