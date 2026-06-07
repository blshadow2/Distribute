package lawSystem.member;

import lawSystem.consultation.ConsultationRequest;
import lawSystem.retainer.RetainerRequest;
import lawSystem.legalCase.CaseInfo;

import java.util.ArrayList;
import java.util.List;

public class Lawyer extends Member {
    protected String lawyerId;
    protected String licenseNumber;
    protected String officeLocation;
    protected List<String> specialty;
    protected int currentWorkload;
    protected String introduction;

    protected List<ConsultationRequest> consultationRequests;
    protected List<RetainerRequest> retainerRequests;

    public Lawyer(
            String memberId,
            String lawyerId,
            String name,
            String email,
            String password,
            String phoneNumber,
            String licenseNumber,
            String officeLocation,
            List<String> specialty,
            String introduction
    ) {
        super(memberId, name, email, password, phoneNumber, "LAWYER");
        this.lawyerId = lawyerId;
        this.licenseNumber = licenseNumber;
        this.officeLocation = officeLocation;
        this.specialty = specialty != null ? specialty : new ArrayList<>();
        this.currentWorkload = 0;
        this.introduction = introduction;
        this.consultationRequests = new ArrayList<>();
        this.retainerRequests = new ArrayList<>();
    }

    public void updateLawyerInfo(
            String officeLocation,
            List<String> specialty,
            String introduction
    ) {
        if (officeLocation != null && !officeLocation.trim().isEmpty()) {
            this.officeLocation = officeLocation;
        }

        if (specialty != null) {
            this.specialty = specialty;
        }

        if (introduction != null && !introduction.trim().isEmpty()) {
            this.introduction = introduction;
        }
    }

    public CaseInfo viewCaseInfo(String caseId) {
        if (caseId == null || caseId.trim().isEmpty()) {
            return null;
        }

        /*
         * 실제 DB 또는 Repository가 있다면
         * caseId를 기준으로 CaseInfo를 조회해서 반환하는 부분이다.
         */
        return null;
    }

    public List<ConsultationRequest> viewConsultationRequest() {
        return consultationRequests;
    }

    public List<RetainerRequest> viewRetainerRequest() {
        return retainerRequests;
    }

    public boolean addConsultationRequest(ConsultationRequest request) {
        if (request == null) {
            return false;
        }

        consultationRequests.add(request);
        return true;
    }

    public boolean addRetainerRequest(RetainerRequest request) {
        if (request == null) {
            return false;
        }

        retainerRequests.add(request);
        return true;
    }

    protected RetainerRequest findRetainerRequest(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            return null;
        }

        for (RetainerRequest request : retainerRequests) {
            if (requestId.equals(request.getRetainerRequestId())) {
                return request;
            }
        }

        return null;
    }

    protected ConsultationRequest findConsultationRequest(String requestId) {
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

    public void increaseWorkload() {
        currentWorkload++;
    }

    public void decreaseWorkload() {
        if (currentWorkload > 0) {
            currentWorkload--;
        }
    }

    public String getLawyerId() {
        return lawyerId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public List<String> getSpecialty() {
        return specialty;
    }

    public int getCurrentWorkload() {
        return currentWorkload;
    }

    public String getIntroduction() {
        return introduction;
    }
}
