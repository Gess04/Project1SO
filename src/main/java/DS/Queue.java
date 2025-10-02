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
public class Queue<T> {

    public static class Node<T> {

        T data;
        Node<T> next;

        public Node(T data) {
            this.data = data;
            this.next = null;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public Node<T> getNext() {
            return next;
        }

        public void setNext(Node<T> next) {
            this.next = next;
        }
    }

    private Node<T> front; 
    private Node<T> rear; 
    private int size;

    public Queue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int getSize() {
        return size;
    }

    public void enqueue(T data) {
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            front = newNode; 
            rear = newNode;
        } else {
            rear.next = newNode; 
            rear = newNode;      
        }
        size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty.");
        }
        T data = front.data;     
        front = front.next;      
        if (front == null) {     
            rear = null;
        }
        size--;
        return data;
    }

    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return front.data;
    }

    public T peekRear() {
        if (isEmpty()) {
            return null;
        }
        return rear.data;
    }

    public Node<T>[] getAllNodes() {
        Node<T>[] array = (Node<T>[]) new Node[size]; 
        Node<T> current = front;
        int index = 0;

        while (current != null) {
            array[index++] = current;
            current = current.next;
        }
        return array;
    }

    public Process[] getAllElements() {
        Process[] elements = new Process[size]; 
        Node<T> current = front;               
        int index = 0;
        while (current != null) {
            elements[index++] = (Process) current.data; 
            current = current.next;
        }
        return elements;
    }

    // Clear the queue
    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }

    public void printQueue() {
        Node<T> current = front;
        while (current != null) {
            System.out.print(current.data.toString() + " ");  // Print the data of each element
            current = current.next;
            System.out.println("");
        }
        System.out.println();
    }

    public T dequeueById(Integer id) {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty.");
        }

        Node<T> current = front;
        Node<T> previous = null;

        while (current != null) {
            if (current.data instanceof Process && ((Process) current.data).getID().equals(id)) {
                if (previous == null) {
                    return dequeue();
                } else {
                    previous.next = current.next;
                    if (current == rear) {
                        rear = previous;
                    }
                    size--;
                    return current.data;
                }
            }
            previous = current;
            current = current.next;
        }
        return null;
    }

}
