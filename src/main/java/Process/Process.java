/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Process;
import Clock.ClockManager;
import Settings.Settings;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Gabriel Flores
 */
public class Process extends Thread {

    public Process(String name, String instructionsCounter, String exceptionCycle) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
   
    public enum Status {
        Running,
        Blocked,
        Ready,
        Exit
    }

    private Integer ID;
    private String processName;
    private Integer instructionCount;
    private Integer remainingBurstTime;
    private boolean CPUbound;
    private boolean IObound;
    private Integer cyclesToExcept;
    private Integer cyclesToCompleteRequest;
    private Status status;
    private Integer PC;
    private Integer MAR;
    private Integer priority;
    private Semaphore mutex;
    private int arrivaltime;
    private double responseRatio;
    
    public Process(int ID, String processName, int instructionCount, Integer remainingBurstTime, boolean CPUbound, boolean IObound, Integer cyclesToExcept, Integer cyclesToCompleteRequest, Status status, Integer PC, Integer MAR, Integer priority, Semaphore mutex, int arrivaltime, double responseRatio) {
        this.ID = ID;
        this.processName = processName;
        this.instructionCount = instructionCount;
        this.remainingBurstTime = remainingBurstTime;
        this.CPUbound = CPUbound;
        this.IObound = IObound;
        this.cyclesToExcept = cyclesToExcept;
        this.cyclesToCompleteRequest = cyclesToCompleteRequest;
        this.status = Status.Ready;
        this.PC = 0;
        this.MAR = 0;
        this.priority = priority;
        this.mutex = mutex;
        this.arrivaltime = arrivaltime;
        this.responseRatio = 0.0;
    }

    public Process(String processName, Integer instructionCount, Integer cyclesToExcept) {
        this.processName = processName;
        this.instructionCount = instructionCount;
        this.cyclesToExcept = cyclesToExcept;
    }
    
    

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public Integer getInstructionCount() {
        return instructionCount;
    }

    public void setInstructionCount(Integer instructionCount) {
        this.instructionCount = instructionCount;
    }

    public Integer getRemainingBurstTime() {
        return remainingBurstTime;
    }

    public void setRemainingBurstTime(Integer remainingBurstTime) {
        this.remainingBurstTime = remainingBurstTime;
    }
    
    public void decreaseRemainingBurstTime() {
        this.remainingBurstTime--;
    }

    public boolean isCPUbound() {
        return CPUbound;
    }

    public void setCPUbound(boolean CPUbound) {
        this.CPUbound = CPUbound;
    }

    public boolean isIObound() {
        return IObound;
    }

    public void setIObound(boolean IObound) {
        this.IObound = IObound;
    }

    public Integer getCyclesToExcept() {
        return cyclesToExcept;
    }

    public void setCyclesToExcept(Integer cyclesToExcept) {
        this.cyclesToExcept = cyclesToExcept;
    }

    public Integer getCyclesToCompleteRequest() {
        return cyclesToCompleteRequest;
    }

    public void setCyclesToCompleteRequest(Integer cyclesToCompleteRequest) {
        this.cyclesToCompleteRequest = cyclesToCompleteRequest;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
  

    public Integer getPC() {
        return PC;
    }

    public void setPC(Integer PC) {
        this.PC = PC;
    }

    public Integer getMAR() {
        return MAR;
    }

    public void setMAR(Integer MAR) {
        this.MAR = MAR;
    }

    public Semaphore getMutex() {
        return mutex;
    }

    public void setMutex(Semaphore mutex) {
        this.mutex = mutex;
    }

    public int getArrivaltime() {
        return arrivaltime;
    }

    public void setArrivaltime(int arrivaltime) {
        this.arrivaltime = arrivaltime;
    }

    public double getResponseRatio() {
        return responseRatio;
    }

    public void setResponseRatio(double responseRatio) {
        this.responseRatio = responseRatio;
    }
    
    public void printProcessDetails() {
        System.out.println("Process ID:" + ID);
        System.out.println("Name:" + processName);
        System.out.println("Instruction Count:" + instructionCount);
        System.out.println("Remaining Burst time:" + remainingBurstTime);
        System.out.println("CPU Bound:" + CPUbound);
        System.out.println("I/O Bound:" + IObound);
        System.out.println("# Cycles for exception:" + cyclesToExcept);
        System.out.println("# Cycles to complete the request:" + cyclesToCompleteRequest);
        System.out.println("Status:" + status);
        System.out.println("Program Counter (PC):" + PC);
        System.out.println("Memory Address Register (MAR):" + MAR);
    }

    @Override
    public String toString() {
        return "Process{" + "processName=" + processName + ", instructionCount=" + instructionCount + '}';
    }
    
    public void saveToCSV(double instDur) {
        File file = new File("ExecutionAlgorithm.csv");
        System.out.println("Guardando CSV en:" + file.getAbsolutePath());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\MiProyecto\\ExecutionAlgorithm.csv"))) {
            bw.write("Process,InstructionDuration,Algorithm");
            bw.newLine();
            bw.write(this.processName + "," + this.instructionCount + "," + this.cyclesToExcept);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
     public static Settings loadFromCSV() {
        try (BufferedReader br = new BufferedReader(new FileReader("ExecutionAlgorithm.csv"))) {
            br.readLine(); // saltar encabezado
            String line = br.readLine();
            if (line != null) {
                String[] values = line.split(",");
                int cpus = Integer.parseInt(values[0]);
                double duration = Double.parseDouble(values[1]);
                String algorithm = values[2];
                ClockManager clock = new ClockManager(duration);
                
                return new Settings(cpus, duration, algorithm,clock);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ClockManager defaultClock = new ClockManager(1.0);
        return new Settings(1, 1.0, "FCFS",defaultClock); // valores por defecto si no hay archivo
    }
    
}
