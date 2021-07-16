package bgu.spl.net.impl.rci;

public interface Command<T> {

    String[] execute(T arg, String[] strings);
}
