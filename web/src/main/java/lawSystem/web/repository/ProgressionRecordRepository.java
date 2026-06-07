package lawSystem.web.repository;

import java.util.List;

import lawSystem.jpa.entity.ProgressionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressionRecordRepository extends JpaRepository<ProgressionRecord, String> {

    /** 의뢰인의 사건들에 기록된 진행상황(최신순). */
    List<ProgressionRecord> findByLegalCase_Client_MemberIdOrderByProgressIdDesc(String memberId);

    /** 변호사 담당 사건들의 진행상황(최신순). */
    List<ProgressionRecord> findByLegalCase_AssignedLawyer_MemberIdOrderByProgressIdDesc(String memberId);
}
