/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DS;
import Process.Process;
/**
 *
 * @author Gabriel Flores
 */
public class ProcessList {
    
    private static class Node {
        Process data;
        Node next;
        public Node (Process data){
            this.data = data;
            this.next = null;
        }
    }
    private Node head;
    private Node tail;
    private int size;
    
    public ProcessList(){
        this.head = null;
        this.tail = null;
        this.size = 0;
        
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public int getSize() {
        return size;
    }
    
    public void add(Process process){
        Node newNode = new Node (process);
        if (isEmpty()){
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }
    
    public void remove (int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of range");
        }
        if (index ==0){
            head = head.next;
            if(head==null){
                tail = null;
            }
        } else{
            Node current = head;
            for (int i =0; i < index -1; i++) {
                current = current.next;
            }
            current.next = current.next.next;
            if (current.next == null){
                tail = current;
            }
        }
    }
}
