package account_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "principle_groups")
public class Group implements Comparable<Group> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String code;
    private String name;
    private String role;
    @JsonIgnore
    @ManyToMany(mappedBy = "userGroups")
    private List<User> users = new ArrayList<>();


    @Override
    public int compareTo(Group o) {
        return this.code.compareToIgnoreCase(o.getCode());
    }
}
