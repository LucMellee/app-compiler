package nl.han.ica.datastructures;

import java.util.Iterator;

public class HANLinkedList<T> implements IHANLinkedList<T> {
    private HANListNode<T> header;
    private int size = 0;

    public HANLinkedList() {
        this.header = new HANListNode<T>();
    }

    @Override
    public void addFirst(T value) {
        HANListNode<T> newHANListNode = new HANListNode<T>(value);
        newHANListNode.next = header.next;
        this.header.next = newHANListNode;
        size++;
    }

    @Override
    public void clear() {
        header.next = null;
    }

    @Override
    public void insert(int index, T value) {
        HANListNode<T> newHANListNode = new HANListNode<>();
        newHANListNode.element = value;
        HANListNode<T> temp = header;

        for (int i = 0; i < Integer.min(index, this.size); i++) {
            temp = temp.next;
        }

        newHANListNode.next = temp.next;
        temp.next = newHANListNode;
        size++;
    }

    @Override
    public void delete(int pos) {
        HANListNode<T> itr = header.next;
        int i = 0;
        if (pos <= 0){
            itr = header;
        }
        else {
            while (itr != null && i < pos - 1) {
                itr = itr.next;
                i++;
            }
        }
        itr.next = itr.next.next;
        size--;
    }

    @Override
    public T get(int pos) {
        HANListNode<T> itr = header.next;
        int i = 0;
        while (itr!=null && i<pos){
            itr = itr.next;
            i++;
        }
        return itr.element;
    }

    @Override
    public void removeFirst() {
        this.header.next = this.header.next.next;
        size--;
    }

    @Override
    public T getFirst() {
        return this.header.next.element;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public String toString() {
        return "HANLinkedList{" +
                "\nheader{element=" + header.element + ", next=" + header.next + "}";
    }
}
