package lawSystem.web.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lawSystem.consultation.ConsultationStatus;
import lawSystem.consultation.ScheduleStatus;
import lawSystem.jpa.entity.Client;
import lawSystem.jpa.entity.ConsultationRequest;
import lawSystem.jpa.entity.ConsultationSchedule;
import lawSystem.jpa.entity.LegalCase;
import lawSystem.jpa.entity.Lawyer;
import lawSystem.jpa.entity.Member;
import lawSystem.web.dto.ConsultationDto;
import lawSystem.web.dto.ScheduleDto;
import lawSystem.web.repository.CaseRepository;
import lawSystem.web.repository.ConsultationRequestRepository;
import lawSystem.web.repository.ConsultationScheduleRepository;
import lawSystem.web.repository.LawyerRepository;
import lawSystem.web.repository.MemberRepository;

@Service
public class ConsultationService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ConsultationScheduleRepository scheduleRepository;
    private final ConsultationRequestRepository requestRepository;
    private final LawyerRepository lawyerRepository;
    private final MemberRepository memberRepository;
    private final CaseRepository caseRepository;

    public ConsultationService(ConsultationScheduleRepository scheduleRepository,
                               ConsultationRequestRepository requestRepository,
                               LawyerRepository lawyerRepository,
                               MemberRepository memberRepository,
                               CaseRepository caseRepository) {
        this.scheduleRepository = scheduleRepository;
        this.requestRepository = requestRepository;
        this.lawyerRepository = lawyerRepository;
        this.memberRepository = memberRepository;
        this.caseRepository = caseRepository;
    }

    // ---------- 일정 (사무직원) ----------
    @Transactional
    public void registerSchedule(String lawyerId, LocalDateTime dateTime, int duration) {
        Lawyer lawyer = lawyerRepository.findById(lawyerId)
                .orElseThrow(() -> new IllegalArgumentException("변호사를 찾을 수 없습니다."));
        scheduleRepository.save(new ConsultationSchedule(
                "sch-" + UUID.randomUUID().toString().substring(0, 8), lawyer, dateTime, duration));
    }

    @Transactional(readOnly = true)
    public List<ScheduleDto> listSchedules() {
        return scheduleRepository.findAllByOrderByDateTimeAsc().stream().map(this::toScheduleDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleDto> availableSchedules() {
        return scheduleRepository.findByAvailableStatusOrderByDateTimeAsc(ScheduleStatus.AVAILABLE)
                .stream().map(this::toScheduleDto).toList();
    }

    // ---------- 상담 신청 (의뢰인) ----------
    @Transactional
    public void requestConsultation(String clientId, String caseId, String lawyerId,
                                    String scheduleId, String memo) {
        Member m = memberRepository.findById(clientId).orElse(null);
        Client client = (m instanceof Client c) ? c : null;
        LegalCase legalCase = (caseId != null && !caseId.isBlank())
                ? caseRepository.findById(caseId).orElse(null) : null;
        Lawyer lawyer = lawyerRepository.findById(lawyerId)
                .orElseThrow(() -> new IllegalArgumentException("변호사를 찾을 수 없습니다."));
        ConsultationSchedule schedule = (scheduleId != null && !scheduleId.isBlank())
                ? scheduleRepository.findById(scheduleId).orElse(null) : null;

        requestRepository.save(new ConsultationRequest(
                "con-" + UUID.randomUUID().toString().substring(0, 8),
                legalCase, client, lawyer, schedule, memo));

        if (schedule != null) {
            schedule.setAvailableStatus(ScheduleStatus.RESERVED);
            scheduleRepository.save(schedule);
        }
    }

    @Transactional(readOnly = true)
    public List<ConsultationDto> listForClient(String clientId) {
        return requestRepository.findByClient_MemberIdOrderByRequestedAtDesc(clientId)
                .stream().map(this::toConsultationDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultationDto> listAll() {
        return requestRepository.findAllByOrderByRequestedAtDesc().stream().map(this::toConsultationDto).toList();
    }

    @Transactional
    public void approve(String requestId) {
        requestRepository.findById(requestId).ifPresent(r -> r.setRequestStatus(ConsultationStatus.APPROVED));
    }

    @Transactional
    public void reject(String requestId) {
        requestRepository.findById(requestId).ifPresent(r -> {
            r.setRequestStatus(ConsultationStatus.REJECTED);
            if (r.getSchedule() != null) {
                r.getSchedule().setAvailableStatus(ScheduleStatus.AVAILABLE);
            }
        });
    }

    /** 의뢰인 본인의 거절/취소된 상담 신청만 삭제한다. */
    @Transactional
    public void deleteByClient(String requestId, String clientId) {
        requestRepository.findById(requestId).ifPresent(r -> {
            boolean owner = r.getClient() != null && clientId.equals(r.getClient().getMemberId());
            boolean deletable = r.getRequestStatus() == ConsultationStatus.REJECTED
                    || r.getRequestStatus() == ConsultationStatus.CANCELED;
            if (owner && deletable) {
                requestRepository.delete(r);
            }
        });
    }

    private ScheduleDto toScheduleDto(ConsultationSchedule s) {
        return new ScheduleDto(
                s.getScheduleId(),
                s.getLawyer() != null ? s.getLawyer().getMemberId() : null,
                s.getLawyer() != null ? s.getLawyer().getName() : "-",
                s.getDateTime() != null ? s.getDateTime().format(FMT) : "-",
                s.getDuration(),
                s.getAvailableStatus() != null ? s.getAvailableStatus().name() : null);
    }

    private ConsultationDto toConsultationDto(ConsultationRequest r) {
        return new ConsultationDto(
                r.getConsultationRequestId(),
                r.getLegalCase() != null ? r.getLegalCase().getTitle() : "-",
                r.getClient() != null ? r.getClient().getName() : "-",
                r.getLawyer() != null ? r.getLawyer().getName() : "-",
                r.getSchedule() != null && r.getSchedule().getDateTime() != null
                        ? r.getSchedule().getDateTime().format(FMT) : "미정",
                r.getRequestStatus() != null ? r.getRequestStatus().name() : null,
                r.getRequestMemo(),
                r.getRequestedAt() != null ? r.getRequestedAt().format(FMT) : "-");
    }
}
