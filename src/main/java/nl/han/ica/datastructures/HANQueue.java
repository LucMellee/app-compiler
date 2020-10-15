package nl.han.ica.datastructures;

public class HANQueue<T> implements IHANQueue<T> {
    private HANLinkedList<T> list = new HANLinkedList<>();

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean isEmpty() {
        return list.getSize() < 1;
    }

    @Override
    public void enqueue(T value) {
        list.insert(list.getSize(), value);
    }

    @Override
    public T dequeue() {
        T value = list.getFirst();
        list.removeFirst();
        return value;
    }

    @Override
    public T peek() {
        return list.getFirst();
    }

    @Override
    public int getSize() {
        return list.getSize();
    }

    @Override
    public String toString() {
        return list.toString();
    }
}