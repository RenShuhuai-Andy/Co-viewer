function clickNotifyTopTab(){
	
	notifyTopTab=document.getElementById("notifyTopTab");
	
	if (!notifyTopTab.classList.contains("on")) {
		friendsTopTab=document.getElementById("friendsTopTab");
		recentTopTab=document.getElementById("recentTopTab");
		notifyTopTab.classList.toggle("on");
		friendsTopTab.classList.toggle("right");
		if (friendsTopTab.classList.contains("on")) {
			friendsTopTab.classList.toggle("on");
		} else{
			friendsTopTab.classList.toggle("left");
		}
		if (recentTopTab.classList.contains("on")) {
			recentTopTab.classList.toggle("on");
		} 
	}
}

function clickFriendsTopTab(){
	
	friendsTopTab=document.getElementById("friendsTopTab");
	
	if (!friendsTopTab.classList.contains("on")) {
		notifyTopTab=document.getElementById("notifyTopTab");
		recentTopTab=document.getElementById("recentTopTab");
		if (notifyTopTab.classList.contains("on")) {
			notifyTopTab.classList.toggle("on");
		} 
		friendsTopTab.classList.toggle("on");
		if (friendsTopTab.classList.contains("right")) {
			friendsTopTab.classList.toggle("right");
		} else{
			friendsTopTab.classList.toggle("left");
		}
		if (recentTopTab.classList.contains("on")) {
			recentTopTab.classList.toggle("on");
		} 
	}
}

function clickRecentTopTab(){
	
	recentTopTab=document.getElementById("recentTopTab");
	
	if (!recentTopTab.classList.contains("on")) {
		notifyTopTab=document.getElementById("notifyTopTab");
		friendsTopTab=document.getElementById("friendsTopTab");
		if (notifyTopTab.classList.contains("on")) {
			notifyTopTab.classList.toggle("on");
		} 
		friendsTopTab.classList.toggle("left");
		if (friendsTopTab.classList.contains("on")) {
			friendsTopTab.classList.toggle("on");
		} else{
			friendsTopTab.classList.toggle("right");
		}
		recentTopTab.classList.toggle("on");
	}
}

function clickFold(){
	recentTopTab=document.getElementById("recentTopTab");
	notifyTopTab=document.getElementById("notifyTopTab");
	friendsTopTab=document.getElementById("friendsTopTab");
	fold=document.getElementById("fold");
	buddy=document.getElementById("buddy");
	webpager=document.getElementById("webpager");
	backBtm=document.getElementById("backBtm");
	
	fold.style.display="none";
	recentTopTab.style.display="table";
	notifyTopTab.style.display="table";
	friendsTopTab.style.display="table";
	buddy.style.display="block";
	webpager.classList.remove("fold");
	webpager.style.width="240px";
	backBtm.style.display="block";
}

function overWenpager(){
	backBtm=document.getElementById("backBtm");
	backBtm.style.left="-40px";
}

function clickBack(){
	recentTopTab=document.getElementById("recentTopTab");
	notifyTopTab=document.getElementById("notifyTopTab");
	friendsTopTab=document.getElementById("friendsTopTab");
	fold=document.getElementById("fold");
	buddy=document.getElementById("buddy");
	webpager=document.getElementById("webpager");
	backBtm=document.getElementById("backBtm");
	
	fold.style.display="block";
	recentTopTab.style.display="none";
	notifyTopTab.style.display="none";
	friendsTopTab.style.display="none";
	buddy.style.display="none";
	webpager.classList.add("fold");
	webpager.style.width="70px";
	backBtm.style.display="none";
}
