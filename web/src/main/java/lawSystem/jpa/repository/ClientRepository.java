package lawSystem.jpa.repository;

import lawSystem.jpa.JpaManager;
import lawSystem.jpa.entity.Client;

import java.util.Optional;

public class ClientRepository extends BaseRepository<Client, String> {

    public ClientRepository() {
        super(Client.class);
    }

    public Optional<Client> findByEmail(String email) {
        return JpaManager.query(em ->
                em.createQuery("SELECT c FROM Client c WHERE c.email = :email", Client.class)
                        .setParameter("email", email)
                        .getResultStream()
                        .findFirst()
        );
    }
}
