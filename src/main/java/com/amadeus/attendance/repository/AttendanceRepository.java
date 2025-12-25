package com.amadeus.attendance.repository;

import com.amadeus.attendance.model.Attendance;
import com.amadeus.attendance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // Find attendance for a specific user on a specific date
    Optional<Attendance> findByUserAndAttendanceDate(User user, LocalDate date);

    // Find all attendance records for a user in a date range
    List<Attendance> findByUserAndAttendanceDateBetween(User user, LocalDate startDate, LocalDate endDate);

    // Count attendance by category for a user in a date range
    @Query("SELECT a.category.name, COUNT(a) FROM Attendance a " +
            "WHERE a.user = :user AND a.attendanceDate BETWEEN :startDate AND :endDate " +
            "GROUP BY a.category.name")
    List<Object[]> countByCategoryForUserInDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
