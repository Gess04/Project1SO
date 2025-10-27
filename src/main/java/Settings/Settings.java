/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Settings;

import Clock.ClockManager;
import java.io.*;
import java.nio.file.*;

/**
 *
 * @author Gabriel Flores
 */
public class Settings {
    private double instructionDuration;
    private String planningAlgorithm;
    private ClockManager clockManager;

    public Settings(double instructionDuration, String planningAlgorithm, ClockManager clockManager) {
        this.instructionDuration = instructionDuration;
        this.planningAlgorithm = planningAlgorithm;
        this.clockManager = clockManager;
    }
    
    public double getInstructionDuration() {
        return instructionDuration;
    }

    public void setInstructionDuration(double instructionDuration) {
        this.instructionDuration = instructionDuration;
    }

    public String getPlanningAlgorithm() {
        return planningAlgorithm;
    }

    public void setPlanningAlgorithm(String planningAlgorithm) {
        this.planningAlgorithm = planningAlgorithm;
    }

    public ClockManager getClockManager() {
        return clockManager;
    }

    public void setClockManager(ClockManager clockManager) {
        this.clockManager = clockManager;
    }


    public void printSettings(){
        System.out.println("Instruction Duration: " + instructionDuration);
        System.out.println("Planning Algorithm: " + planningAlgorithm);
    }


    private static final String CSV_REL_PATH = "src/main/java/Settings/ExecutionAlgorithm.csv";
    
    private static File resolveCsvFile() {
        String projectRoot = System.getProperty("user.dir");
        File file = Paths.get(projectRoot, CSV_REL_PATH).toFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return file;
    }

    public void saveToCSV() {
        File file = resolveCsvFile();
        System.out.println("[Settings] Guardando CSV en: " + file.getAbsolutePath());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            // Encabezado + una sola fila con la configuración actual
            bw.write("InstructionDuration,Algorithm");
            bw.newLine();
            bw.write(this.instructionDuration + "," + this.planningAlgorithm);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("[Settings] Error guardando CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Settings loadFromCSV() {
        File file = resolveCsvFile();
        System.out.println("[Settings] Cargando CSV desde: " + file.getAbsolutePath());

        if (!file.exists()) {
            System.out.println("[Settings] No existe CSV, usando valores por defecto.");
            ClockManager defaultClock = new ClockManager(1.0);
            return new Settings(1.0, "FCFS", defaultClock);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String header = br.readLine(); // salta encabezado
            String line = br.readLine();   // primera (y única) fila
            if (line != null) {
                String[] values = line.split(",");
//                int cpus = Integer.parseInt(values[0].trim());
                double duration = Double.parseDouble(values[0].trim());
                String algorithm = values[1].trim();

                ClockManager clock = new ClockManager(duration);
                return new Settings(duration, algorithm, clock);
            } else {
                System.out.println("[Settings] CSV vacío, usando valores por defecto.");
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("[Settings] Error leyendo CSV: " + e.getMessage());
            e.printStackTrace();
        }

        ClockManager fallbackClock = new ClockManager(1.0);
        return new Settings(1.0, "FCFS", fallbackClock);
    }
}