package db;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import io.ebean.Finder;
import io.ebean.Model;

@Entity
public class Scenario extends Model {
	
	@Id
    public Long id;

	@ManyToOne
	public Farm farm;
	
	// all Farms should have one and only one baseline. 
	public Boolean isBaseline;
	public String scenarioName; // "Baseline" when isBaseline is true, not editable in that case...
	
    @OneToMany(cascade = CascadeType.ALL)
    public List<Field> mFields = new ArrayList<>();

    public static final Finder<Long, Scenario> find = new Finder<>(Scenario.class);
    
}