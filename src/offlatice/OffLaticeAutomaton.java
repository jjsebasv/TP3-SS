package offlatice;

import cellindexmethod.CellIndexMethod;
import models.DynamicParticle;
import models.Particle;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.MarshalledObject;
import java.util.*;
import java.util.List;

/**
 * Created by sebastian on 3/18/17.
 */
public class OffLaticeAutomaton extends CellIndexMethod {

    private double n;
    private double vel;
    private Map<Integer, List<Particle>> simulation = new HashMap<>();
    private Map<Integer, Double> vaEvolution = new HashMap<>();
    double vaSum = 0;

    public Map<Integer, Double> getvaEvolutions() {
        return vaEvolution;
    }

    public OffLaticeAutomaton(double l, double rc, List<Particle> particles, boolean periodicBoundry, double n, double vel) {
        super(l, rc, particles, periodicBoundry);
        this.n = n;
        this.vel = vel;
        simulation.put(0, particles);
    }


    private void calculateDistances(int T) {
        Map<Particle, Set<Particle>> neighbours = findNeighbors(simulation.get(T));

        List<Particle> nextStep = new ArrayList<>();
        double vx = 0;
        double vy = 0;

        for (Particle p : simulation.get(T)) {
            DynamicParticle dp = (DynamicParticle) p;
            double sinSum = Math.sin(dp.getAngle());
            double cosSum = Math.cos(dp.getAngle());
            Set<Particle> set = neighbours.get(p);
            for (Particle n : set) {
                DynamicParticle dn = (DynamicParticle) n;
                sinSum += Math.sin(dn.getAngle());
                cosSum += Math.cos(dn.getAngle());
            }
            double newX = p.getPosition().x + Math.cos(dp.getAngle()) * dp.getVelocity();
            double newY = p.getPosition().y + Math.sin(dp.getAngle()) * dp.getVelocity();

            if (newX < 0)
                newX += l;
            else if (newX >= l)
                newX -= l;
            if (newY < 0)
                newY += l;
            else if (newY >= l)
                newY -= l;

            double newAngle = Math.atan2(sinSum / (set.size() + 1), cosSum / (set.size() + 1))
                    + (Math.random() * n - (n / 2));

            vx += Math.cos(dp.getAngle()) * dp.getVelocity();
            vy += Math.sin(dp.getAngle()) * dp.getVelocity();

            nextStep.add(new DynamicParticle(p.getId(), p.getRadius(), p.getRc(), newX, newY, newAngle, dp.getVelocity()));
        }
        if(T > 1500 - 150) {
            vaSum += Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2)) / (this.particles.size() * vel);
        }
        simulation.put(T + 1, nextStep);
    }


    public void simulate(int TMax, boolean print, double n, double d) {
        int T = 0;
        while (T < TMax) {
            reloadMatrix(simulation.get(T));
            calculateDistances(T);
            T++;
            if(T % 100 == 0)
                System.out.print(".");
        }
        System.out.println("\nVa: " + vaSum/150);
        if (print) createSimulationFile(n, d);
    }

    private void createSimulationFile(double n, double d) {
        try {
            PrintWriter painter = new PrintWriter("OffLaticeSimulation-n" + n + "-d" + String.format("%.02f", d) + ".xyz", "UTF-8");
            for (Integer t : simulation.keySet()) {
                painter.println(particles.size());
                painter.println(t);
                for (Particle p : simulation.get(t)) {
                    DynamicParticle dp = (DynamicParticle) p;
                    painter.println(dp.toString());
                }
            }
            painter.close();
        } catch (Exception e) {

        }
    }
}
