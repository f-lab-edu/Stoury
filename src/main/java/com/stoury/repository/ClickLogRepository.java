package com.stoury.repository;

import com.stoury.domain.ClickLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClickLogRepository extends JpaRepository<ClickLog, Long> {
}
