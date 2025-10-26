/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package Scheduler;
import CPU.CPU;

/**
 *
 * @author Gabriel Flores
 */
public interface SchedulingAlgorithm {
    void reorder();
    void dispatch(CPU cpu);
    void onTick(CPU cpu);
}
