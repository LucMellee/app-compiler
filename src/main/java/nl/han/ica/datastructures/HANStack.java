package nl.han.ica.datastructures;

public class HANStack<T> implements IHANStack<T> {
    private HANLinkedList<T> list = new HANLinkedList<>();

    @Override
    public void push(T value) {
        list.insert(list.getSize(), value);
    }

    @Override
    public T pop() {
        int size = list.getSize();
        if (size < 1) {
            return null;
        }
        T value = list.get(size - 1);
        list.delete(size - 1);
        return value;
    }

    @Override
    public T peek() {
        int size = list.getSize();
        if (size < 1) {
            return null;
        }
        return list.get(size - 1);
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
