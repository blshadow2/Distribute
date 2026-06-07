package lawSystem.web.repository;

import java.util.List;

import lawSystem.consultation.ScheduleStatus;
import lawSystem.jpa.entity.ConsultationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationScheduleRepository extends JpaRepository<ConsultationSchedule, String> {

    List<ConsultationSchedule> findAllByOrderByDateTimeAsc();

    List<ConsultationSchedule> findByAvailableStatusOrderByDateTimeAsc(ScheduleStatus status);
}
