package lawSystem.jpa.repository;

import lawSystem.jpa.JpaManager;
import lawSystem.jpa.entity.PartnerLawyer;

import java.util.List;

public class PartnerLawyerRepository extends BaseRepository<PartnerLawyer, String> {

    public PartnerLawyerRepository() {
        super(PartnerLawyer.class);
    }

    public List<PartnerLawyer> findBySpecialty(String specialty) {
        return JpaManager.query(em ->
                em.createQuery(
                        "SELECT DISTINCT p FROM PartnerLawyer p JOIN p.specialty s WHERE s = :specialty",
                        PartnerLawyer.class
                ).setParameter("specialty", specialty).getResultList()
        );
    }
}
