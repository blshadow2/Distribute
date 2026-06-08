package lawSystem.web.dto;

import java.util.List;

/** 변호사 검색 결과 표시용. */
public class LawyerDto {

    private final String lawyerId;     // = member_id
    private final String name;
    private final String type;         // 대표변호사 / 소속변호사
    private final String officeLocation;
    private final int currentWorkload;
    private final String introduction;
    private final List<String> specialties;
    private Double matchScore;   // 키워드(의미) 검색 시 유사도 점수, 그 외 null

    public LawyerDto(String lawyerId, String name, String type, String officeLocation,
                     int currentWorkload, String introduction, List<String> specialties) {
        this.lawyerId = lawyerId;
        this.name = name;
        this.type = type;
        this.officeLocation = officeLocation;
        this.currentWorkload = currentWorkload;
        this.introduction = introduction;
        this.specialties = specialties;
    }

    public String getLawyerId() { return lawyerId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getOfficeLocation() { return officeLocation; }
    public int getCurrentWorkload() { return currentWorkload; }
    public String getIntroduction() { return introduction; }
    public List<String> getSpecialties() { return specialties; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }
}
