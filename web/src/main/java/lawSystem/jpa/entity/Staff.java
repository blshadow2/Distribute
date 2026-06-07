package lawSystem.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "staff")
@PrimaryKeyJoinColumn(name = "member_id")
public class Staff extends Member {

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "position", length = 100)
    private String position;

    protected Staff() {
        super();
    }

    public Staff(
            String memberId,
            String name,
            String email,
            String password,
            String phoneNumber,
            String department,
            String position
    ) {
        super(memberId, name, email, password, phoneNumber, "STAFF");
        this.department = department;
        this.position = position;
    }

    public String getDepartment() { return department; }
    public String getPosition() { return position; }

    public void setDepartment(String department) { this.department = department; }
    public void setPosition(String position) { this.position = position; }
}
