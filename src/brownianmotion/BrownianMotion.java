package brownianmotion;

import cellindexmethod.CellIndexMethod;
import com.oracle.tools.packager.Log;
import com.sun.tools.javac.util.Pair;
import models.Collision;
import models.CollisionedWith;
import models.MassParticle;
import models.Particle;
import sun.applet.resources.MsgAppletViewer_sv;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by sebastian on 4/6/17.
 */
public class BrownianMotion extends CellIndexMethod {

    private List<List<Particle>> simulation = new ArrayList<>();

    private final double TIME_GRANULARITY = 0.0005;

    private final double EPSILON = 0.000001;

    private int simulationTime;

    private int percentDiff;

    private int framesJump;

    private int cant = 0;

    private int lastRemovedFrame = 1;


    public BrownianMotion(double l, double rc, List<Particle> particles, int time) {
        super(l, rc, particles, false);
        simulation.add(particles);
        simulationTime = time;
        percentDiff = (int) (time / TIME_GRANULARITY / 100);
        framesJump = (int) (1 / TIME_GRANULARITY / 60);
    }

    public List<List<Particle>> simulate(double time) {
        double t = 0.0;
        List<Particle> currentState = simulation.get(simulation.size() - 1);
        while (t < time) {


            reloadMatrix(currentState);

            Collision collision = getNextCollision(currentState);
            double tc = collision.getTc();
            while (tc > TIME_GRANULARITY) {
                List<Particle> intermediateState = evolveSystem(currentState, collision);
                t += TIME_GRANULARITY;

                simulation.add(intermediateState);

                reloadMatrix(intermediateState);
                currentState = intermediateState;
                tc -= TIME_GRANULARITY;
                collision.setTc(tc);
                cant++;
                showPercentage();
            }

            currentState = simulation.get(simulation.size() - 1);
            List<Particle> nextState = evolveSystem(currentState, collision);
            t += TIME_GRANULARITY;

            simulation.add(nextState);

            currentState = nextState;
            cant++;
            showPercentage();
        }

        return simulation;
    }

    // Private functions
    private void showPercentage() {
        if (cant % percentDiff == 0) {
            System.out.println(cant / percentDiff + " %");
        }

        if ((cant % (percentDiff * 10) == 0) ) {
            removeUnusedFrames();
        }
    }

    private void removeUnusedFrames() {
        int simulationSize = simulation.size();
        int i;
        for (i = lastRemovedFrame; i < simulationSize; i++) {
            if(i % framesJump != 0 && i != simulationSize-1) {
                simulation.get(i).clear();
            }
        }
        lastRemovedFrame = simulation.size();
    }

    private Collision getNextCollision(List<Particle> particles) {

        Map<Particle, Set<Particle>> neighbours = findNeighbors(particles);
        Collision firstCollision = null;

        for (Particle p : particles) {
            MassParticle mp = (MassParticle) p;

            Collision collision = getMinTcOfPaticle(mp, neighbours.get(mp));

            if (particles.get(0) == p) {
                firstCollision = collision;
            } else if (firstCollision.getTc() > collision.getTc()) {
                firstCollision = collision;
            }
        }

        return firstCollision;
    }

    private List<Particle> evolveSystem(List<Particle> particles, Collision collision) {

        ArrayList<Particle> nextParticles = new ArrayList<>();

        for (Particle p : particles) {
            MassParticle mp = (MassParticle) p;
            double newX = mp.getPosition().x + mp.getVx() * TIME_GRANULARITY;
            double newY = mp.getPosition().y + mp.getVy() * TIME_GRANULARITY;

            if (newX >= l) {
                newX -= (newX - l) + EPSILON + mp.getRadius();
            }
            if (newY >= l) {
                newY -= (newY - l) + EPSILON + mp.getRadius();
            }
            if (newX < 0) {
                newX += (newX * -1) + EPSILON + mp.getRadius();
            }
            if (newY < 0) {
                newY += (newY * -1) + EPSILON + mp.getRadius();
            }

            Pair<Double, Double> newVel = handleCollision(mp, collision);

            MassParticle newMp = new MassParticle(mp.getId(), mp.getRadius(), mp.getRc(), newX, newY, newVel.fst, newVel.snd, mp.getMass());

            nextParticles.add(newMp);
        }

        return nextParticles;
    }

    private Pair<Double, Double> handleCollision(MassParticle mp, Collision collision) {
        double newVx = mp.getVx();
        double newVy = mp.getVy();

        if (TIME_GRANULARITY >= collision.getTc() &&
                (mp.equals(collision.getParticles().fst) || mp.equals(collision.getParticles().snd))) {
            switch (collision.getCollisionedWith()) {
                case HORIZONTAL_WALL:
                    newVy = mp.getVy() * -1;
                    newVx = mp.getVx();
                    break;
                case VERTICAL_WALL:
                    newVy = mp.getVy();
                    newVx = mp.getVx() * -1;
                    break;
                case PARTICLE:
                    MassParticle counterpart =
                            mp.equals(collision.getParticles().fst) ? collision.getParticles().snd : collision.getParticles().fst;
                    return mp.getVelAfterCollision(counterpart);

            }
        }

        return new Pair<>(newVx, newVy);
    }

    private Pair<Double, MassParticle> getParticleCollisionTime(MassParticle particle, Set<Particle> neighbours) {
        MassParticle colissionedWith = particle;
        double minTc = -1;

        for (Particle n : neighbours) {
            MassParticle neighbour = (MassParticle) n;
            if (particle.equals(neighbour))
                continue;

            double timeToCol = MassParticle.timeToCollision(particle, neighbour);
            if (minTc == -1) {
                minTc = timeToCol;
                colissionedWith = neighbour;
            } else if (minTc > timeToCol) {
                minTc = timeToCol;
                colissionedWith = neighbour;
            }
        }

        return new Pair<>(minTc, colissionedWith);

    }

    private double getVerticlaWallCollisionTime(MassParticle particle) {
        if (particle.getVx() > 0) {
            return (this.l - particle.getRadius() - particle.getPosition().x) / particle.getVx();
        } else if (particle.getVx() < 0) {
            return (particle.getRadius() - particle.getPosition().x) / particle.getVx();
        }
        return Double.MAX_VALUE;
    }

    private double getHorizontalWallCollisionTime(MassParticle particle) {
        if (particle.getVy() > 0) {
            return (this.l - particle.getRadius() - particle.getPosition().y) / particle.getVy();
        } else if (particle.getVy() < 0) {
            return (particle.getRadius() - particle.getPosition().y) / particle.getVy();
        }
        return Double.MAX_VALUE;
    }

    private Collision getMinTcOfPaticle(MassParticle mp, Set<Particle> neighbours) {
        ArrayList<Double> tcs = new ArrayList<>();

        tcs.add(getVerticlaWallCollisionTime(mp));
        tcs.add(getHorizontalWallCollisionTime(mp));

        Pair<Double, MassParticle> particleTc = getParticleCollisionTime(mp, neighbours);
        tcs.add(particleTc.fst);

        double minTc = Collections.min(tcs);

        if (tcs.indexOf(minTc) == CollisionedWith.VERTICAL_WALL.ordinal()) {
            return new Collision(minTc, CollisionedWith.VERTICAL_WALL, new Pair<>(mp, null));
        } else if (tcs.indexOf(minTc) == CollisionedWith.HORIZONTAL_WALL.ordinal()) {
            return new Collision(minTc, CollisionedWith.HORIZONTAL_WALL, new Pair<>(mp, null));
        } else {
            return new Collision(minTc, CollisionedWith.PARTICLE, new Pair<>(mp, particleTc.snd));
        }
    }

    public void createSimulationFile(List<List<Particle>> simulation, double vel) {
        try {
            PrintWriter painter = new PrintWriter("BrownianSimulation vel: " + String.format("%.02f", vel) + ".xyz", "UTF-8");
            for (int i = 0; i < simulation.size(); i++) {
                if(simulation.get(i).size() == 0) continue;
                painter.println(simulation.get(0).size());
                painter.println(i);
                for (Particle p : simulation.get(i)) {
                    MassParticle mp = (MassParticle) p;
                    painter.println(mp.toString());
                }
            }
            painter.close();
        } catch (Exception e) {
            Log.debug(e);
        }
    }

}
