package org.placeholder.homerback.entities.modules;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy= InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="discriminator", discriminatorType= DiscriminatorType.STRING)
@DiscriminatorValue("MODULE")
public abstract class AbstractModule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private EModuleType type;

    public AbstractModule(EModuleType type){
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EModuleType getType() {
        return type;
    }

    public void setType(EModuleType type) {
        this.type = type;
    }
}
