package lawSystem.jpa.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * 변호사 추상 엔티티. PartnerLawyer / AssociateLawyer 가 상속한다.
 * specialty 는 List&lt;String&gt; 이므로 별도 lawyer_specialty 테이블로 매핑된다.
 */
@Entity
@Table(name = "lawyer")
@PrimaryKeyJoinColumn(name = "member_id")
public abstract class Lawyer extends Member {

    @Column(name = "license_number", length = 50, nullable = false)
    protected String licenseNumber;

    @Column(name = "office_location", length = 255)
    protected String officeLocation;

    @Column(name = "current_workload", nullable = false)
    protected int currentWorkload = 0;

    @Column(name = "introduction", columnDefinition = "TEXT")
    protected String introduction;

    @ElementCollection
    @CollectionTable(
            name = "lawyer_specialty",
            joinColumns = @JoinColumn(name = "member_id")
    )
    @Column(name = "specialty", length = 100)
    protected List<String> specialty = new ArrayList<>();

    protected Lawyer() {
        super();
    }

    protected Lawyer(
            String memberId,
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
        this.licenseNumber = licenseNumber;
        this.officeLocation = officeLocation;
        this.specialty = specialty != null ? new ArrayList<>(specialty) : new ArrayList<>();
        this.introduction = introduction;
    }

    public String getLicenseNumber() { return licenseNumber; }
    public String getOfficeLocation() { return officeLocation; }
    public int getCurrentWorkload() { return currentWorkload; }
    public String getIntroduction() { return introduction; }
    public List<String> getSpecialty() { return specialty; }

    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public void setOfficeLocation(String officeLocation) { this.officeLocation = officeLocation; }
    public void setCurrentWorkload(int currentWorkload) { this.currentWorkload = currentWorkload; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    public void setSpecialty(List<String> specialty) { this.specialty = specialty; }
}
