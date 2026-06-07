package lawSystem.member;

import lawSystem.legalCase.Case;
import lawSystem.legalCase.CaseInfo;
import lawSystem.legalCase.Evidence;

public class Client extends Member {
    private String clientId;
    private String address;
    private int registeredCaseCount;
    private boolean identityVerified;

    public Client(
            String memberId,
            String clientId,
            String name,
            String email,
            String password,
            String phoneNumber,
            String address
    ) {
        super(memberId, name, email, password, phoneNumber, "CLIENT");
        this.clientId = clientId;
        this.address = address;
        this.registeredCaseCount = 0;
        this.identityVerified = false;
    }

    public Case inputCaseInfo(CaseInfo caseInfo) {
        if (!isLoggedIn()) {
            System.out.println("사건 정보를 입력하려면 로그인이 필요합니다.");
            return null;
        }

        if (caseInfo == null) {
            System.out.println("입력된 사건 정보가 없습니다.");
            return null;
        }

        if (!caseInfo.validateRequiredFields()) {
            System.out.println("필수 사건 정보가 누락되었습니다.");
            return null;
        }

        boolean saved = caseInfo.saveCaseInfo();

        if (!saved) {
            System.out.println("사건 정보 저장에 실패했습니다.");
            return null;
        }

        Case createdCase = Case.createCase(caseInfo);

        if (createdCase != null) {
            registeredCaseCount++;
        }

        return createdCase;
    }

    public boolean uploadEvidence(String caseId, Evidence evidence) {
        if (!isLoggedIn()) {
            System.out.println("증거자료를 등록하려면 로그인이 필요합니다.");
            return false;
        }

        if (caseId == null || caseId.trim().isEmpty()) {
            System.out.println("사건 ID가 없습니다.");
            return false;
        }

        if (evidence == null) {
            System.out.println("등록할 증거자료가 없습니다.");
            return false;
        }

        return true;
    }

    public void setIdentityVerified(boolean identityVerified) {
        this.identityVerified = identityVerified;
    }

    public String getClientId() {
        return clientId;
    }

    public String getAddress() {
        return address;
    }

    public int getRegisteredCaseCount() {
        return registeredCaseCount;
    }

    public boolean isIdentityVerified() {
        return identityVerified;
    }
}