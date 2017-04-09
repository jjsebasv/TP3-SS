import brownianmotion.BrownianMotion;
import com.oracle.tools.packager.Log;
import models.MassParticle;
import models.Particle;

import java.io.*;
import java.util.*;


/**
 * Created by amounier on 3/12/17.
 */
public class main {

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

        // N - L - RC - n - v - T
        int N = 200;
        double L = 0.5;
        double vel = 0.1;
        double Rc = 0.5;
        double radius = 0.005;
        double mass = 0.1;
        double time = 60;

        List<Particle> particles = generateRandomBrownianMotionState(N, L, radius, Rc, vel, mass);

        BrownianMotion brownianMotion = new BrownianMotion(L, Rc, particles);

        List<List<Particle>> simulation = brownianMotion.simulate(time);

        createSimulationFile(simulation, vel);

    }

    private static List<Particle> generateRandomBrownianMotionState(int cantParticles, double l, double radius, double rc, double vel, double mass) {
        List<Particle> particles = new ArrayList<>(cantParticles);
        Random r = new Random();
        for (int i = 0; i < cantParticles; i++) {
            double x = l * r.nextDouble();
            double y = l * r.nextDouble();
            double vx = (Math.random() * 2 * vel) - vel;
            double vy = (Math.random() * 2 * vel) - vel;
            MassParticle particle = new MassParticle(i, radius, rc, x, y, vx, vy, mass);
            while (!isValid(particle, particles)) {
                x = l * r.nextDouble();
                y = l * r.nextDouble();
                particle = new MassParticle(i, radius, rc, x, y, vx, vy, mass);
            }
            particles.add(particle);
        }
        return particles;
    }

    public static void createSimulationFile(List<List<Particle>> simulation, double vel) {
        try {
            PrintWriter painter = new PrintWriter("BrownianSimulation vel: " + String.format("%.02f", vel) + ".xyz", "UTF-8");
            for (int i = 0; i < simulation.size(); i+=33) {
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


    public static boolean isValid(Particle p, List<? extends Particle> particles) {
        for (Particle p2 : particles) {
            if (Particle.getDistance(p, p2) < 0)
                return false;
        }
        return true;
    }
}



