package lawSystem.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "partner_lawyer")
@PrimaryKeyJoinColumn(name = "member_id")
public class PartnerLawyer extends Lawyer {

    @Column(name = "managing_lawyer_id", length = 64)
    private String managingLawyerId;

    protected PartnerLawyer() {
        super();
    }

    public PartnerLawyer(
            String memberId,
            String managingLawyerId,
            String name,
            String email,
            String password,
            String phoneNumber,
            String licenseNumber,
            String officeLocation,
            List<String> specialty,
            String introduction
    ) {
        super(memberId, name, email, password, phoneNumber, licenseNumber, officeLocation, specialty, introduction);
        this.managingLawyerId = managingLawyerId;
    }

    public String getManagingLawyerId() { return managingLawyerId; }

    public void setManagingLawyerId(String managingLawyerId) { this.managingLawyerId = managingLawyerId; }
}
