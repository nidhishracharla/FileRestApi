package com.ge.capital.dms.fr.sle.controllers.api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.util.List;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFileVersion;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem.Info;
import com.box.sdk.BoxTrash;
import com.ge.capital.dms.utility.DmsUtilityService;

public class BoxCopy {

	public static void main(String[] args) throws IOException {
		// moveCompleted();
		DmsUtilityService dmsUtilityService = new DmsUtilityService();
		String boxToken = dmsUtilityService.requestAccessToken();
		BoxAPIConnection api = new BoxAPIConnection(boxToken);
		java.net.Proxy proxy = new java.net.Proxy(Type.HTTP,
				new InetSocketAddress("PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com", 80));
		api.setProxy(proxy);
		BoxCopy.getfileversion(api);
	}


	public static void getfileversion(BoxAPIConnection api) {
		BoxFile file = new BoxFile(api,"749011390408");
		
		System.out.println(""+file.getInfo().getVersionNumber());
		System.out.println("SHA1 of \"%s\": %s\n"+ file.getInfo().getVersion().getID());
		//List<BoxFileVersion> versions = (List<BoxFileVersion>) file.getVersions();
//		for(BoxFileVersion version : versions){
//			
//		}
	}
	private static void moveCompleted() {
		DmsUtilityService dmsUtilityService = new DmsUtilityService();
		String boxToken = dmsUtilityService.requestAccessToken();
		// System.out.println(boxToken);
		BoxAPIConnection api = new BoxAPIConnection(boxToken);
		java.net.Proxy proxy = new java.net.Proxy(Type.HTTP,
				new InetSocketAddress("PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com", 80));
		api.setProxy(proxy);

		BoxFolder fldr = new BoxFolder(api, "115731185981"); // folder to be moved

		BoxFolder rootFldr = new BoxFolder(api, "113424140759");
		for (Info childFldr : rootFldr.getChildren()) {
			if (childFldr instanceof BoxFolder.Info) {
				if (childFldr.getName().contains("-Complete")) {
					BoxFolder toBeMoved = new BoxFolder(api, childFldr.getID());
					toBeMoved.move(fldr);
				}
			}
		}

	}

}
