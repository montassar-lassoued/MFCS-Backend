package com.IntraConnect.path.srm;

import com.IntraConnect.storageSystem.StorageSystem;

import java.util.List;

public class SRM {
	
	String id;
	List<Fork> forks;
	StorageSystem storageSystem;
	public String getId() {
		return id;
	}
	
	public List<Fork> getForks() {
		return forks;
	}
	
	public String getEntryPointForFork(String srm_id, String storagePoint) {
		// Logik: Welcher Fork erreicht dieses Fach? -> Gib den Entry zurück.
		// <Entry>*</Entry>
		return storageSystem.getEntryPoint(srm_id, forks, storagePoint);

	}
	
	public String getExitPointForFork(String srm_id, String storagePoint) {
		// <Exit>*</Exit>
		return storageSystem.getExitPoint(srm_id, forks, storagePoint);
	}
}
