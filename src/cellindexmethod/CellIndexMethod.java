package cellindexmethod;

import models.DynamicParticle;
import models.Particle;
import models.Point;

import java.util.*;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by sebastian on 3/12/17.
 */
public class CellIndexMethod {

    Set<Particle>[][] matrix;

    protected double cellLenght;

    protected double l;
    protected double rc;
    protected int m;

    protected List<? extends Particle> particles;
    protected boolean periodicBoundry = false;

    public CellIndexMethod(double l, double rc, List<? extends Particle> particles, boolean periodicBoundry) {
        this.l = l;
        this.rc = rc;
        this.m = (int) Math.floor(l / rc);
        cellLenght = l / m;
        this.particles = particles;
        this.periodicBoundry = periodicBoundry;
        insertParticles(m, particles);
    }

    private void insertParticles(int m, List<? extends Particle> particles) {
        matrix = new Set[m][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                matrix[i][j] = new HashSet<>();
            }
        }
        for (Particle p :
                particles) {
            Point position = p.getPosition();
            matrix[(int) (position.x / cellLenght)][(int) (position.y / cellLenght)].add(p);
        }
    }

    public Map<Particle, Set<Particle>> findNeighbors(List<Particle> particles) {

        Map<Particle, Set<Particle>> map = new HashMap<>();

        for (Particle particle : particles) {

            if (!map.containsKey(particle))
                map.put(particle, new HashSet<>());

            Point coords = particle.getPosition();
            int x = (int) (coords.x / cellLenght);
            int y = (int) (coords.y / cellLenght);
            Set<Particle> cell;

            cell = matrix[x][y];
            addNeighbors(cell, particle, map, 0, 0);

            cell = matrix[(x - 1 + m) % m][y];
            if (x - 1 >= 0) {
                addNeighbors(cell, particle, map, 0, 0);
            } else if (periodicBoundry) {
                addNeighbors(cell, particle, map, -1, 0);
            }

            cell = matrix[(x - 1 + m) % m][(y + 1) % m];
            if (x - 1 >= 0 && y + 1 < m) {
                addNeighbors(cell, particle, map, 0, 0);
            } else if (periodicBoundry) {
                addNeighbors(cell, particle, map, x - 1 >= 0 ? 0 : -1, y + 1 < m ? 0 : 1);
            }

            cell = matrix[x][(y + 1) % m];
            if (y + 1 < m) {
                addNeighbors(cell, particle, map, 0, 0);
            } else if (periodicBoundry) {
                addNeighbors(cell, particle, map, 0, 1);
            }

            cell = matrix[(x + 1) % m][(y + 1) % m];
            if (x + 1 < m && y + 1 < m) {
                addNeighbors(cell, particle, map, 0, 0);
            } else if (periodicBoundry) {
                addNeighbors(cell, particle, map, x + 1 < m ? 0 : 1, y + 1 < m ? 0 : 1);
            }
        }

        return map;
    }

    private void addNeighbors(Set<Particle> c, Particle p, Map<Particle, Set<Particle>> m, int deltaX,
                              int deltaY) {

        for (Particle candidate : c) {
            if (!candidate.equals(p) && !m.get(p).contains(candidate)) {
                double distance = Math.max(getDistance(p, candidate, deltaX, deltaY), 0);
                if (distance <= rc) {
                    m.get(p).add(candidate);
                    if (!m.containsKey(candidate))
                        m.put(candidate, new HashSet<>());
                    m.get(candidate).add(p);
                }
                
            }
        }
    }

    private double getDistance(Particle p1, Particle p2, int deltaX, int deltaY) {
        return Math
                .sqrt(Math.pow(p1.getPosition().x - (p2.getPosition().x + deltaX * l), 2) + Math.pow(p1.getPosition().y - (p2.getPosition().y + deltaY * l), 2))
                - p1.getRadius() - p2.getRadius();
    }

    protected void reloadMatrix(List<Particle> particles) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                matrix[i][j] = new HashSet<>();
            }
        }

        for (Particle p : particles) {
            int x = (int) (p.getPosition().x / cellLenght);
            int y = (int) (p.getPosition().y / cellLenght);
            matrix[x][y].add(p);
        }
    }

}
