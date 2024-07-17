package service.vaxapp.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import service.vaxapp.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    @Query(value = "SELECT * FROM notification WHERE notification.user_to_notify_id=:userId AND is_read=0", nativeQuery = true)
    List<Notification> findUnread(Integer userId);

    @Query(value = "SELECT * FROM notification WHERE notification.user_to_notify_id=:userId", nativeQuery = true)
    List<Notification> findAll(Integer userId);
}
