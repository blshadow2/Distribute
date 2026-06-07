package lawSystem.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 회원 베이스 엔티티. JOINED 상속 전략을 사용한다.
 * - 모든 자식(Client/Lawyer/Staff) 은 자신의 테이블에 member_id 를 PK 로 공유한다.
 * - role 컬럼은 단순 표기용 (Hibernate 가 자동 구분하므로 discriminator 는 사용하지 않음).
 */
@Entity
@Table(name = "member")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Member {

    @Id
    @Column(name = "member_id", length = 64, nullable = false, updatable = false)
    protected String memberId;

    @Column(name = "name", length = 100, nullable = false)
    protected String name;

    @Column(name = "email", length = 150, nullable = false, unique = true)
    protected String email;

    @Column(name = "password", length = 255, nullable = false)
    protected String password;

    @Column(name = "phone_number", length = 30)
    protected String phoneNumber;

    @Column(name = "role", length = 20, nullable = false)
    protected String role;

    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt = LocalDateTime.now();

    protected Member() {
        // JPA 용 기본 생성자
    }

    protected Member(
            String memberId,
            String name,
            String email,
            String password,
            String phoneNumber,
            String role
    ) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setRole(String role) { this.role = role; }
}
