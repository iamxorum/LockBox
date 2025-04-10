package com.lockbox.domain.service;

import java.util.List;
import java.util.Optional;

public interface CrudService<T, ID> {

    List<T> findAll();

    Optional<T> findById(ID id);

    T save(T entity);

    void delete(T entity);
    
    void deleteById(ID id);
    
    boolean existsById(ID id);
} 