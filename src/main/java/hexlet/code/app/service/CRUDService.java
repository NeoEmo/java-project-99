package hexlet.code.app.service;

import java.util.List;

public interface CRUDService<T, X, Y, Z> {

    default List<T> index() {
        return index(0, Integer.MAX_VALUE);
    }

    default List<T> index(int page, int size) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    default List<T> index(Z params, int page, int size) {
        return index(page, size);
    }

    T findById(Long id);

    T create(X data); // ...CreateDTO data

    T update(Y data, Long id); // ...PatchDTO data

    void delete(Long id);
}
