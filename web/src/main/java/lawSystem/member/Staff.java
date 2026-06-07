package lawSystem.member;

import lawSystem.consultation.ConsultationRequest;
import lawSystem.consultation.ConsultationSchedule;

import java.util.ArrayList;
import java.util.List;

public class Staff extends Member {
    private String staffId;
    private String department;
    private String position;

    private List<ConsultationSchedule> consultationSchedules;
    private List<ConsultationRequest> consultationRequests;

    public Staff(
            String memberId,
            String staffId,
            String name,
            String email,
            String password,
            String phoneNumber,
            String department,
            String position
    ) {
        super(memberId, name, email, password, phoneNumber, "STAFF");
        this.staffId = staffId;
        this.department = department;
        this.position = position;
        this.consultationSchedules = new ArrayList<>();
        this.consultationRequests = new ArrayList<>();
    }

    public ConsultationSchedule registerConsultationSchedule(
            ConsultationSchedule schedule
    ) {
        if (!isLoggedIn()) {
            System.out.println("상담 일정을 등록하려면 로그인이 필요합니다.");
            return null;
        }

        if (schedule == null) {
            System.out.println("등록할 상담 일정이 없습니다.");
            return null;
        }

        consultationSchedules.add(schedule);

        System.out.println("상담 일정이 등록되었습니다.");
        return schedule;
    }

    public boolean updateConsultationSchedule(
            String scheduleId,
            ConsultationSchedule schedule
    ) {
        if (!isLoggedIn()) {
            System.out.println("상담 일정을 수정하려면 로그인이 필요합니다.");
            return false;
        }

        if (scheduleId == null || scheduleId.trim().isEmpty()) {
            return false;
        }

        if (schedule == null) {
            return false;
        }

        ConsultationSchedule targetSchedule = findConsultationSchedule(scheduleId);

        if (targetSchedule == null) {
            System.out.println("수정할 상담 일정을 찾을 수 없습니다.");
            return false;
        }

        return targetSchedule.updateSchedule(
                schedule.getDateTime(),
                schedule.getDuration()
        );
    }

    public boolean approveConsultationRequest(String requestId) {
        if (!isLoggedIn()) {
            System.out.println("상담 신청에 응답하려면 로그인이 필요합니다.");
            return false;
        }

        ConsultationRequest request = findConsultationRequest(requestId);

        if (request == null) {
            System.out.println("상담 신청을 찾을 수 없습니다.");
            return false;
        }

        boolean approved = request.approveRequest();

        if (approved) {
            ConsultationSchedule schedule = findConsultationSchedule(request.getScheduleId());

            if (schedule != null) {
                schedule.reserveSchedule();
            }

            System.out.println("상담 신청을 수락했습니다.");
        }

        return approved;
    }

    public boolean rejectConsultationRequest(
            String requestId,
            String reason
    ) {
        if (!isLoggedIn()) {
            System.out.println("상담 신청에 응답하려면 로그인이 필요합니다.");
            return false;
        }

        ConsultationRequest request = findConsultationRequest(requestId);

        if (request == null) {
            System.out.println("상담 신청을 찾을 수 없습니다.");
            return false;
        }

        boolean rejected = request.rejectRequest();

        if (rejected) {
            System.out.println("상담 신청을 거절했습니다.");
            System.out.println("거절 사유: " + reason);
        }

        return rejected;
    }

    public boolean addConsultationRequest(ConsultationRequest request) {
        if (request == null) {
            return false;
        }

        consultationRequests.add(request);
        return true;
    }

    private ConsultationSchedule findConsultationSchedule(String scheduleId) {
        if (scheduleId == null || scheduleId.trim().isEmpty()) {
            return null;
        }

        for (ConsultationSchedule schedule : consultationSchedules) {
            if (scheduleId.equals(schedule.getScheduleId())) {
                return schedule;
            }
        }

        return null;
    }

    private ConsultationRequest findConsultationRequest(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            return null;
        }

        for (ConsultationRequest request : consultationRequests) {
            if (requestId.equals(request.getConsultationRequestId())) {
                return request;
            }
        }

        return null;
    }

    public String getStaffId() {
        return staffId;
    }

    public String getDepartment() {
        return department;
    }

    public String getPosition() {
        return position;
    }

    public List<ConsultationSchedule> getConsultationSchedules() {
        return consultationSchedules;
    }

    public List<ConsultationRequest> getConsultationRequests() {
        return consultationRequests;
    }
}
