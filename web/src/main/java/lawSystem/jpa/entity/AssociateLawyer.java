package lawSystem.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "associate_lawyer")
@PrimaryKeyJoinColumn(name = "member_id")
public class AssociateLawyer extends Lawyer {

    protected AssociateLawyer() {
        super();
    }

    public AssociateLawyer(
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
        super(memberId, name, email, password, phoneNumber, licenseNumber, officeLocation, specialty, introduction);
    }
}
