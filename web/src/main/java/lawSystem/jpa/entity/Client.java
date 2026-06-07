package lawSystem.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "client")
@PrimaryKeyJoinColumn(name = "member_id")
public class Client extends Member {

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "registered_case_count", nullable = false)
    private int registeredCaseCount = 0;

    @Column(name = "identity_verified", nullable = false)
    private boolean identityVerified = false;

    protected Client() {
        super();
    }

    public Client(
            String memberId,
            String name,
            String email,
            String password,
            String phoneNumber,
            String address
    ) {
        super(memberId, name, email, password, phoneNumber, "CLIENT");
        this.address = address;
    }

    public String getAddress() { return address; }
    public int getRegisteredCaseCount() { return registeredCaseCount; }
    public boolean isIdentityVerified() { return identityVerified; }

    public void setAddress(String address) { this.address = address; }
    public void setRegisteredCaseCount(int registeredCaseCount) { this.registeredCaseCount = registeredCaseCount; }
    public void setIdentityVerified(boolean identityVerified) { this.identityVerified = identityVerified; }
}
