package nextvest.repository;

import nextvest.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUsuarioIdOrderByDateTimeDesc(Integer userId);
    List<Order> findByUsuarioIdAndTypeOrderByDateTimeDesc(Integer userId, String type);
}