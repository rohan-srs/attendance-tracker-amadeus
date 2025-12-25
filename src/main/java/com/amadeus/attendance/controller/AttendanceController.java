package com.amadeus.attendance.controller;

import com.amadeus.attendance.model.Attendance;
import com.amadeus.attendance.model.Category;
import com.amadeus.attendance.model.User;
import com.amadeus.attendance.repository.AttendanceRepository;
import com.amadeus.attendance.repository.CategoryRepository;
import com.amadeus.attendance.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public AttendanceController(AttendanceRepository attendanceRepository,
                                UserRepository userRepository,
                                CategoryRepository categoryRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    // Get all attendance records
    @GetMapping("/attendances")
    List<Attendance> getAllAttendances() {
        return attendanceRepository.findAll();
    }

    // Get attendance for a specific user on a specific date
    @GetMapping("/attendance")
    ResponseEntity<?> getAttendanceByUserAndDate(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Optional<Attendance> attendance = attendanceRepository.findByUserAndAttendanceDate(
                user.get(), date);
        return attendance.map(response -> ResponseEntity.ok().body(response))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Get all attendance records for a user in a month
    @GetMapping("/attendance/monthly")
    ResponseEntity<?> getMonthlyAttendance(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month) {

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Attendance> attendances = attendanceRepository.findByUserAndAttendanceDateBetween(
                user.get(), startDate, endDate);

        return ResponseEntity.ok().body(attendances);
    }

    // Get monthly statistics (counts of WFH, WFO, Absence)
    @GetMapping("/attendance/monthly/stats")
    ResponseEntity<?> getMonthlyStats(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month) {

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Object[]> results = attendanceRepository.countByCategoryForUserInDateRange(
                user.get(), startDate, endDate);

        // Convert results to a readable map
        Map<String, Object> stats = new HashMap<>();
        stats.put("year", year);
        stats.put("month", month);
        stats.put("totalDays", startDate.lengthOfMonth());
        stats.put("wfoGoalPercentage", user.get().getWfoGoalPercentage());

        int wfoCount = 0;
        int wfhCount = 0;
        int absenceCount = 0;

        for (Object[] result : results) {
            String categoryName = (String) result[0];
            Long count = (Long) result[1];

            switch (categoryName) {
                case "WFO":
                    wfoCount = count.intValue();
                    break;
                case "WFH":
                    wfhCount = count.intValue();
                    break;
                case "Absence":
                    absenceCount = count.intValue();
                    break;
            }
        }

        stats.put("wfoCount", wfoCount);
        stats.put("wfhCount", wfhCount);
        stats.put("absenceCount", absenceCount);
        stats.put("totalRecorded", wfoCount + wfhCount + absenceCount);

        // Calculate percentage (excluding absences from total days)
        int effectiveDays = startDate.lengthOfMonth() - absenceCount;
        double achievedPercentage = effectiveDays > 0 ?
                (wfoCount * 100.0 / effectiveDays) : 0;
        stats.put("achievedPercentage", Math.round(achievedPercentage * 100.0) / 100.0);

        // Calculate required WFO days based on goal
        int requiredWfoDays = (int) Math.ceil(
                effectiveDays * user.get().getWfoGoalPercentage() / 100.0);
        stats.put("requiredWfoDays", requiredWfoDays);
        stats.put("remainingWfoDays", Math.max(0, requiredWfoDays - wfoCount));

        return ResponseEntity.ok().body(stats);
    }

    // Create or update attendance for a specific date
    @PostMapping("/attendance")
    ResponseEntity<?> createOrUpdateAttendance(@Valid @RequestBody AttendanceRequest request)
            throws URISyntaxException {

        Optional<User> user = userRepository.findById(request.getUserId());
        if (user.isEmpty()) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Optional<Category> category = Optional.ofNullable(categoryRepository.findByName(request.getCategoryName()));
        if (category.isEmpty()) {
            return new ResponseEntity<>("Category not found", HttpStatus.NOT_FOUND);
        }

        // Check if attendance already exists for this date
        Optional<Attendance> existingAttendance = attendanceRepository.findByUserAndAttendanceDate(
                user.get(), request.getAttendanceDate());

        Attendance attendance;
        if (existingAttendance.isPresent()) {
            // Update existing attendance
            attendance = existingAttendance.get();
            attendance.setCategory(category.get());
        } else {
            // Create new attendance
            attendance = new Attendance();
            attendance.setUser(user.get());
            attendance.setCategory(category.get());
            attendance.setAttendanceDate(request.getAttendanceDate());
        }

        Attendance result = attendanceRepository.save(attendance);
        return ResponseEntity.created(new URI("/api/attendance/" + result.getId())).body(result);
    }

    // Delete attendance record
    @DeleteMapping("/attendance/{id}")
    ResponseEntity<?> deleteAttendance(@PathVariable Long id) {
        attendanceRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Inner class for request body
    @Setter
    @Getter
    public static class AttendanceRequest {
        // Getters and setters
        private Long userId;
        private String categoryName;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate attendanceDate;

    }
}