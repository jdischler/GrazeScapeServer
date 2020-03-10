package db;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.annotation.DbArray;

@Entity
public class Rotation extends Model {
	
	@Id
    public Long id;
	
	public String name;
	
    @DbArray
    public Set<Landcover> definition = new HashSet<>();
    
    public Rotation name(String setName) {
    	name = setName;
    	return this;
    }
    
    public Rotation add(Landcover lc) {
    	definition.add(lc);
    	return this;
    }
    
    public static void createDefaults() {
    	
    	Rotation rot = null;
    	
    	rot = new Rotation().name("Continuous Corn").add(Landcover.ECornGrain); 	
    	rot.save();
    	
    	rot = new Rotation().name("CashGrain").add(Landcover.ECornGrain)
    			.add(Landcover.ESoybeans);
    	rot.save();
    	
    	rot = new Rotation().name("Dairy Rotation").add(Landcover.ECornGrain)
    			.add(Landcover.ECornSilage)
    			.add(Landcover.EAlfalfa).add(Landcover.EAlfalfa).add(Landcover.EAlfalfa);
    	rot.save();
    }
    
    public static final Finder<Long, Rotation> find = new Finder<>(Rotation.class);
    
}
