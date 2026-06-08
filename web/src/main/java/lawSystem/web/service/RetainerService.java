package lawSystem.web.service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lawSystem.jpa.entity.Client;
import lawSystem.jpa.entity.LegalCase;
import lawSystem.jpa.entity.Lawyer;
import lawSystem.jpa.entity.Member;
import lawSystem.jpa.entity.RetainerCondition;
import lawSystem.jpa.entity.RetainerRequest;
import lawSystem.consultation.ConsultationStatus;
import lawSystem.legalCase.CaseStatus;
import lawSystem.retainer.ConditionStatus;
import lawSystem.retainer.RetainerStatus;
import lawSystem.web.dto.RetainerDto;
import lawSystem.web.repository.CaseRepository;
import lawSystem.web.repository.ConsultationRequestRepository;
import lawSystem.web.repository.LawyerRepository;
import lawSystem.web.repository.MemberRepository;
import lawSystem.web.repository.RetainerRequestRepository;

/**
 * 수임: 요청(의뢰인) → 조건 전달(대표) → 응답 수락/거절(의뢰인) → 수임(대표).
 */
@Service
public class RetainerService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final RetainerRequestRepository requestRepository;
    private final MemberRepository memberRepository;
    private final CaseRepository caseRepository;
    private final LawyerRepository lawyerRepository;
    private final ConsultationRequestRepository consultationRequestRepository;

    public RetainerService(RetainerRequestRepository requestRepository,
                           MemberRepository memberRepository,
                           CaseRepository caseRepository,
                           LawyerRepository lawyerRepository,
                           ConsultationRequestRepository consultationRequestRepository) {
        this.requestRepository = requestRepository;
        this.memberRepository = memberRepository;
        this.caseRepository = caseRepository;
        this.lawyerRepository = lawyerRepository;
        this.consultationRequestRepository = consultationRequestRepository;
    }

    @Transactional
    public void requestRetainer(String clientId, String caseId, String lawyerId,
                                String scope, int fee, String result, String content) {
        Member m = memberRepository.findById(clientId).orElse(null);
        Client client = (m instanceof Client c) ? c : null;
        LegalCase legalCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("사건을 선택하세요."));
        Lawyer lawyer = lawyerRepository.findById(lawyerId)
                .orElseThrow(() -> new IllegalArgumentException("변호사를 선택하세요."));

        // 유스케이스: 상담이 완료되어야 수임이 가능하다.
        boolean consultationDone = consultationRequestRepository
                .existsByClient_MemberIdAndLawyer_MemberIdAndRequestStatus(
                        clientId, lawyerId, ConsultationStatus.COMPLETED);
        if (!consultationDone) {
            throw new IllegalStateException("상담이 완료된 변호사에게만 수임을 요청할 수 있습니다.");
        }

        RetainerRequest req = new RetainerRequest(
                "ret-" + UUID.randomUUID().toString().substring(0, 8),
                legalCase, client, lawyer, content, scope, fee, result);
        req.setRequestStatus(RetainerStatus.REQUEST_SENT);
        requestRepository.save(req);
    }

    @Transactional(readOnly = true)
    public List<RetainerDto> listForClient(String clientId) {
        return requestRepository.findByClient_MemberIdOrderByRequestedAtDesc(clientId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<RetainerDto> listAll() {
        return requestRepository.findAllByOrderByRequestedAtDesc().stream().map(this::toDto).toList();
    }

    /** 대표변호사: 수임 조건 전달. */
    @Transactional
    public void sendCondition(String reqId, int fee, String scope, String terms) {
        requestRepository.findById(reqId).ifPresent(req -> {
            RetainerCondition cond = new RetainerCondition(
                    "cond-" + UUID.randomUUID().toString().substring(0, 8), fee, scope, terms);
            cond.setConditionStatus(ConditionStatus.SENT);
            req.addCondition(cond);    // cascade ALL 로 함께 저장
            req.setRequestStatus(RetainerStatus.CONDITION_SENT);
            requestRepository.save(req);
        });
    }

    /** 의뢰인: 조건 수락. */
    @Transactional
    public void acceptCondition(String reqId) {
        requestRepository.findById(reqId).ifPresent(req -> {
            req.setRequestStatus(RetainerStatus.CONDITION_ACCEPTED);
            latestCondition(req).ifPresent(c -> c.setConditionStatus(ConditionStatus.ACCEPTED));
        });
    }

    /** 의뢰인: 조건 조정 요청(메시지와 함께). 대표가 다시 조건을 전달할 수 있다. */
    @Transactional
    public void requestAdjustment(String reqId, String message) {
        requestRepository.findById(reqId).ifPresent(req -> {
            if (req.getRequestStatus() == RetainerStatus.CONDITION_SENT) {
                req.setRequestStatus(RetainerStatus.CONDITION_ADJUSTMENT_REQUESTED);
                req.setAdjustmentNote(message);
            }
        });
    }

    /** 의뢰인: 조건 거절. */
    @Transactional
    public void rejectCondition(String reqId) {
        requestRepository.findById(reqId).ifPresent(req -> {
            req.setRequestStatus(RetainerStatus.REJECTED);
            latestCondition(req).ifPresent(c -> c.setConditionStatus(ConditionStatus.REJECTED));
        });
    }

    /** 대표변호사: 수임 확정 → 사건 상태 RETAINED + 담당 변호사 지정. */
    @Transactional
    public void retain(String reqId) {
        requestRepository.findById(reqId).ifPresent(req -> {
            if (req.getRequestStatus() != RetainerStatus.CONDITION_ACCEPTED) {
                return;   // 조건 수락 상태에서만 수임 가능
            }
            req.setRequestStatus(RetainerStatus.RETAINED);
            LegalCase c = req.getLegalCase();
            if (c != null) {
                c.setCaseStatus(CaseStatus.RETAINED);
                c.setAssignedLawyer(req.getLawyer());
                caseRepository.save(c);
            }
        });
    }

    /** 대표변호사: 수임 요청 거절(수임 확정/이미 거절 상태가 아니면 가능). */
    @Transactional
    public void rejectByPartner(String reqId) {
        requestRepository.findById(reqId).ifPresent(req -> {
            RetainerStatus st = req.getRequestStatus();
            if (st == RetainerStatus.RETAINED || st == RetainerStatus.REJECTED) {
                return;
            }
            req.setRequestStatus(RetainerStatus.REJECTED);
            latestCondition(req).ifPresent(c -> c.setConditionStatus(ConditionStatus.REJECTED));
        });
    }

    /** 의뢰인 본인의 거절된 수임 요청만 삭제한다(조건은 cascade 로 함께 삭제). */
    @Transactional
    public void deleteByClient(String requestId, String clientId) {
        requestRepository.findById(requestId).ifPresent(req -> {
            boolean owner = req.getClient() != null && clientId.equals(req.getClient().getMemberId());
            if (owner && req.getRequestStatus() == RetainerStatus.REJECTED) {
                requestRepository.delete(req);
            }
        });
    }

    /** 대표변호사: 거절된 수임 요청 삭제(수임 관리 화면 정리용, 조건은 cascade 로 함께 삭제). */
    @Transactional
    public void deleteByPartner(String requestId) {
        requestRepository.findById(requestId).ifPresent(req -> {
            if (req.getRequestStatus() == RetainerStatus.REJECTED) {
                requestRepository.delete(req);
            }
        });
    }

    private Optional<RetainerCondition> latestCondition(RetainerRequest req) {
        return req.getConditions().stream().max(Comparator.comparing(RetainerCondition::getCreatedAt));
    }

    private RetainerDto toDto(RetainerRequest r) {
        Optional<RetainerCondition> cond = latestCondition(r);
        return new RetainerDto(
                r.getRetainerRequestId(),
                r.getLegalCase() != null ? r.getLegalCase().getCaseId() : null,
                r.getLegalCase() != null ? r.getLegalCase().getTitle() : "-",
                r.getClient() != null ? r.getClient().getName() : "-",
                r.getLawyer() != null ? r.getLawyer().getName() : "-",
                r.getRequestContent(),
                r.getDesiredScope(),
                r.getDesiredFee(),
                r.getDesiredResult(),
                r.getRequestStatus() != null ? r.getRequestStatus().name() : null,
                r.getRequestedAt() != null ? r.getRequestedAt().format(FMT) : "-",
                r.getAdjustmentNote(),
                cond.isPresent(),
                cond.map(RetainerCondition::getFee).orElse(0),
                cond.map(RetainerCondition::getScope).orElse(null),
                cond.map(RetainerCondition::getAdditionalTerms).orElse(null),
                cond.map(c -> c.getConditionStatus().name()).orElse(null));
    }
}
