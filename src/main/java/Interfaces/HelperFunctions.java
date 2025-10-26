/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Interfaces;

import Scheduler.*;
import DS.Queue;
import Clock.ClockManager;
import java.util.concurrent.Semaphore;
import javax.swing.JOptionPane;

/**
 *
 * @author tomasaraujo
 */
public class HelperFunctions {
    
    /**
     * 
     * @param planningAlgorithm
     * @param readyQueue
     * @param clockManager
     * @return Aplica el algoritmo de planificación seleccionado
     */
    public Scheduler updateSchedulerAlgorithm(String planningAlgorithm, Queue readyQueue, ClockManager clockManager, Semaphore readyLock) {
//        String selected = (String) planningAlgorithm.getSelectedItem();
        String selected = planningAlgorithm; 

        switch (selected) {
            case "FCFS":
                System.out.println("Algoritmo cambiado a FCFS");
                JOptionPane.showMessageDialog(null, "✅ Configuración guardada exitosamente.");
                return new Scheduler(new FCFS(readyQueue), readyQueue);
                
            case "SPN":
                System.out.println("Algoritmo cambiado a SPN");
                JOptionPane.showMessageDialog(null, "✅ Configuración guardada exitosamente.");
                return new Scheduler(new SPN(readyQueue), readyQueue);

            case "SRT":
                System.out.println("Algoritmo cambiado a SRT");
                JOptionPane.showMessageDialog(null, "✅ Configuración guardada exitosamente.");
                return new Scheduler(new SRT(readyQueue), readyQueue);

            case "HRRN":
                System.out.println("Algoritmo cambiado a HRRN");
                JOptionPane.showMessageDialog(null, "✅ Configuración guardada exitosamente.");
                return new Scheduler(new HRRN(readyQueue, clockManager), readyQueue);
               
            case "Round robin":
                System.out.println("Algoritmo cambiado a Round Robin");
                JOptionPane.showMessageDialog(null, "✅ Configuración guardada exitosamente.");
                return new Scheduler(new RoundRobin(readyQueue, readyLock, 5), readyQueue);
            
            default:
                System.out.println("Algoritmo no reconocido.");
                break;
        }
        return new Scheduler(new FCFS(readyQueue), readyQueue);
    }
}
