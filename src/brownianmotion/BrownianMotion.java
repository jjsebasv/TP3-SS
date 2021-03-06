package brownianmotion;

import cellindexmethod.CellIndexMethod;

import javafx.util.Pair;
import models.Collision;
import models.CollisionedWith;
import models.MassParticle;
import models.Particle;

import java.io.PrintWriter;
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

    private int cantCollisions = 0;

    private List<Double> collisionTimes = new ArrayList<>();

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
            cantCollisions++;
            collisionTimes.add(tc);
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
        System.out.println();
        //System.out.println("Collision Frequency:\t" + cantCollisions / simulationTime + "\tcol/seg");

        double sum = 0;
        for (Double tc : collisionTimes) {
            sum += tc;
        }

        //System.out.println("Collision time average:\t" + String.format(Locale.FRENCH, "%.10f", sum / collisionTimes.size()));
        //System.out.println("Collision times");

        double twoThirds = (2 * simulationTime) / 3;
        /*/ Print collision times
        double tcAux = 0;
        for(Double tc : collisionTimes) {
            if (tcAux > twoThirds) {
                System.out.println(String.format(Locale.FRENCH, "%.3f", tc*1000));
            }
            tcAux += tc;
        }

        // Print velocities
        printVelocities(twoThirds);
        */
        return simulation;
    }

    private void showPercentage() {
        if (cant % percentDiff == 0) {
            System.out.print(".");
        }

        if ((cant % (percentDiff * 10) == 0)) {
            removeUnusedFrames();
        }
    }

    private void removeUnusedFrames() {
        int simulationSize = simulation.size();
        int i;
        for (i = lastRemovedFrame; i < simulationSize; i++) {
            if (i % framesJump != 0 && i != simulationSize - 1) {
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

            MassParticle newMp = new MassParticle(mp.getId(), mp.getRadius(), mp.getRc(), newX, newY, newVel.getKey(), newVel.getValue(), mp.getMass());

            nextParticles.add(newMp);
        }

        return nextParticles;
    }

    private Pair<Double, Double> handleCollision(MassParticle mp, Collision collision) {
        double newVx = mp.getVx();
        double newVy = mp.getVy();

        if (TIME_GRANULARITY >= collision.getTc() &&
                (mp.equals(collision.getParticles().getKey()) || mp.equals(collision.getParticles().getValue()))) {
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
                            mp.equals(collision.getParticles().getKey()) ? collision.getParticles().getValue() : collision.getParticles().getKey();
                    return mp.getVelAfterCollision(counterpart);

            }
        }

        return new Pair<>(newVx, newVy);
    }

    private Pair<Double, MassParticle> getParticleCollisionTime(MassParticle particle, Set<Particle> neighbours) {
        MassParticle colissionedWith = particle;
        double minTc = Double.MAX_VALUE;

        for (Particle n : neighbours) {
            MassParticle neighbour = (MassParticle) n;
            if (particle.equals(neighbour))
                continue;

            double timeToCol = Math.abs(MassParticle.timeToCollision(particle, neighbour));

            if (minTc > timeToCol) {
                minTc = timeToCol;
                colissionedWith = neighbour;
            }
        }

        return new Pair<>(minTc, colissionedWith);

    }

    private double getVerticlaWallCollisionTime(MassParticle particle) {
        if (particle.getVx() > 0) {
            return Math.abs((this.l - particle.getRadius() - particle.getPosition().x) / particle.getVx());
        } else if (particle.getVx() < 0) {
            return Math.abs((particle.getRadius() - particle.getPosition().x) / particle.getVx());
        }
        return Double.MAX_VALUE;
    }

    private double getHorizontalWallCollisionTime(MassParticle particle) {
        if (particle.getVy() > 0) {
            return Math.abs((this.l - particle.getRadius() - particle.getPosition().y) / particle.getVy());
        } else if (particle.getVy() < 0) {
            return Math.abs((particle.getRadius() - particle.getPosition().y) / particle.getVy());
        }
        return Double.MAX_VALUE;
    }

    private Collision getMinTcOfPaticle(MassParticle mp, Set<Particle> neighbours) {
        ArrayList<Double> tcs = new ArrayList<>();

        tcs.add(getVerticlaWallCollisionTime(mp));
        tcs.add(getHorizontalWallCollisionTime(mp));

        Pair<Double, MassParticle> particleTc = getParticleCollisionTime(mp, neighbours);
        tcs.add(particleTc.getKey());

        double minTc = Collections.min(tcs);

        if (tcs.indexOf(minTc) == CollisionedWith.VERTICAL_WALL.ordinal()) {
            return new Collision(minTc, CollisionedWith.VERTICAL_WALL, new Pair<>(mp, null));
        } else if (tcs.indexOf(minTc) == CollisionedWith.HORIZONTAL_WALL.ordinal()) {
            return new Collision(minTc, CollisionedWith.HORIZONTAL_WALL, new Pair<>(mp, null));
        } else {
            return new Collision(minTc, CollisionedWith.PARTICLE, new Pair<>(mp, particleTc.getValue()));
        }
    }

    private void printVelocities(double twoThirds) {
        int k = 0;
        for (List<Particle> lp : simulation) {
            if (lp.size() > 0 && k > ((twoThirds*simulation.size())/simulationTime)) {
                for (Particle p : lp) {
                    MassParticle mp = (MassParticle) p;
                    System.out.print(String.format(Locale.FRENCH, "%.4f", mp.getVelocityModule()) + "\t");
                }
                System.out.println();
            }
            k++;
        }
    }

    public static void createSimulationFile(List<List<Particle>> simulation, double vel, int N) {
        try {
            PrintWriter painter = new PrintWriter("BrownianSimulation vel: " + String.format("%.02f", vel) + " N: " + N + ".xyz", "UTF-8");
            for (int i = 0; i < simulation.size(); i++) {
                if (simulation.get(i).size() == 0) continue;
                painter.println(simulation.get(0).size());
                painter.println(i);
                for (Particle p : simulation.get(i)) {
                    MassParticle mp = (MassParticle) p;
                    painter.println(mp.toString());
                }
            }
            painter.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
