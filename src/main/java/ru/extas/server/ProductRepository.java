package ru.extas.server;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.extas.model.Product;

import java.util.List;

/**
 * Интерфейс доступа к базе всех типов продуктов
 *
 * @author Valery Orlov
 *         Date: 16.01.14
 *         Time: 18:22
 */
@Repository
@Scope(proxyMode = ScopedProxyMode.INTERFACES)
public interface ProductRepository extends CrudRepository<Product, String> {

	List<Product> findByActive(boolean active);
}