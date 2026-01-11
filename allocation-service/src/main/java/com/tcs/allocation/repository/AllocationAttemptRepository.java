package com.tcs.allocation.repository;

import com.tcs.allocation.entity.AllocationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AllocationAttemptRepository extends JpaRepository<AllocationAttempt, Long> {
    List<AllocationAttempt> findByResourceRequestId(Long requestId);
    List<AllocationAttempt> findByEmployeeId(Long employeeId);
}
