package lawSystem.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_analysis_function")
public class AIAnalysisFunction {

    @Id
    @Column(name = "function_id", length = 64, nullable = false, updatable = false)
    private String functionId;

    @Column(name = "function_name", length = 150)
    private String functionName;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "function_status", length = 30)
    private String functionStatus;

    protected AIAnalysisFunction() {}

    public AIAnalysisFunction(String functionId, String functionName, String modelVersion, String functionStatus) {
        this.functionId = functionId;
        this.functionName = functionName;
        this.modelVersion = modelVersion;
        this.functionStatus = functionStatus;
    }

    public String getFunctionId() { return functionId; }
    public String getFunctionName() { return functionName; }
    public String getModelVersion() { return modelVersion; }
    public String getFunctionStatus() { return functionStatus; }
}
