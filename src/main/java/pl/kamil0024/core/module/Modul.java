package pl.kamil0024.core.module;

public interface Modul {

    boolean startUp();

    boolean shutDown();

    String getName();

    boolean isStart();
    void setStart(boolean bol);

}
