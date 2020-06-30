package pl.kamil0024.core.database.config;

import java.util.List;

public interface Dao<T> {
    T get(String id);
    void save(T toCos);
    List<T> getAll();
}
