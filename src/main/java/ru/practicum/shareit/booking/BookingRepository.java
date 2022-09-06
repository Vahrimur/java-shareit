package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("select b from Booking b where b.booker.id = ?1 order by b.start desc")
    List<Booking> findAllByBookerId(Long bookerId);

    @Query("select b from Booking b where b.booker.id = ?1 and ?2 between b.start and b.end order by b.start desc")
    List<Booking> findAllByBookerIdCurrent(Long bookerId, LocalDateTime now);

    @Query("select b from Booking b where b.booker.id = ?1 and b.start > ?2 order by b.start desc")
    List<Booking> findAllByBookerIdFuture(Long bookerId, LocalDateTime now);

    @Query("select b from Booking b where b.booker.id = ?1 and b.end < ?2 order by b.start desc")
    List<Booking> findAllByBookerIdPast(Long bookerId, LocalDateTime now);

    @Query("select b from Booking b where b.booker.id = ?1 and b.status = ?2 order by b.start desc")
    List<Booking> findAllByBookerIdAndStatus(Long bookerId, BookingStatus status);

    @Query(value = "select b from Booking b where b.item in :items order by b.start desc ")
    List<Booking> findAllByItems(@Param("items") List<Item> items);

    @Query(value = "select b from Booking b where b.item.id = :itemId and b.booker.id = :userId")
    List<Booking> findAllByItemIdAndBookerId(@Param("itemId") Long itemId, @Param("userId") Long userId);

    @Query(value = "select b from Booking b where b.item in :items and b.status = :status order by b.start desc ")
    List<Booking> findAllByItemsAndStatus(@Param("items") List<Item> items, @Param("status") BookingStatus status);

    @Query(value = "select b from Booking b where b.item in :items and :now between b.start and b.end " +
            "order by b.start desc ")
    List<Booking> findAllByItemsCurrent(@Param("items") List<Item> items, @Param("now") LocalDateTime now);

    @Query(value = "select b from Booking b where b.item in :items and b.start > :now order by b.start desc ")
    List<Booking> findAllByItemsFuture(@Param("items") List<Item> items, @Param("now") LocalDateTime now);

    @Query(value = "select b from Booking b where b.item in :items and b.end < :now order by b.start desc ")
    List<Booking> findAllByItemsPast(@Param("items") List<Item> items, @Param("now") LocalDateTime now);

    @Query(value = "select b from Booking b where b.item.id = :id and b.end < :now order by b.end desc ")
    List<Booking> findAllByItemIdAndEndBeforeNow(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Query(value = "select b from Booking b where b.item.id = :id and b.start > :now order by b.end asc ")
    List<Booking> findAllByItemIdAndStartAfterNow(@Param("id") Long id, @Param("now") LocalDateTime now);
}
