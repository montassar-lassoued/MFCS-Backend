package com.IntraConnect.intf;

import com.IntraConnect.path.srm.Fork;

import java.util.List;

public interface IntraConnectSRM {
	
	default String getEntryPoint(String srm_id, List<Fork> forks, String storagePoint) {
		if (forks != null && !forks.isEmpty() && !forks.getFirst().getEntryPoints().isEmpty()) {
			return forks.getFirst().getEntryPoints().getFirst();
		}
		return "NO_ENTRY_FOUND";
	}
	default String getExitPoint(String srm_id, List<Fork> forks, String storagePoint) {
		if (forks != null && !forks.isEmpty() && !forks.getFirst().getExitPoints().isEmpty()) {
			return forks.getFirst().getExitPoints().getFirst();
		}
		return "NO_EXIT_FOUND";
	}
}
