$.ajaxSetup ({  
	cache: false  
});  

var numResultsShowing = 0;
var numResultsPerQuery = 15;
var lastAddedResultDiv = null;
var currentSearchQuery = null;
var currentView = null;
var currentType = 'type_songs';
var maxResultsReached = false;
var videoData = new Array(); // cached info
var uagent = navigator.userAgent.toLowerCase();
var maxErrors = 3; // to skip past
var numErrors = 0;
var currentArtistId = null;
var currentLabelId = null;
var currentReleaseId = null;
var defaultView = null;

function initPage() {
	loadViews();				
}

function loadViews() {
	$.getJSON(  
			"views",  
			{},  
			function(results) {
				var first = true;
				for (key in results) {
					var result = results[key];								
					var navElement = $('<a></a>').attr('id', 'playlist_' + result.id).attr('href', 'javascript:selectView(\'playlist_' + result.id + '\')').text(result.name);
					if (first) {
						currentView = 'playlist_' + result.id;
						defaultView = currentView;
						navElement.addClass('viewLinkSelected');
						first = false;
					} else {
						navElement.addClass('viewLink');
					}
					$("#viewsList").append(navElement);	
				}
				showMoreVideos();
			}  
		);
}

function addVideoResult(result, lastEntry) {
	var a = constructVideoDiv(result, lastEntry);
	$("#injectedResults").append(a);				
	++numResultsShowing;
}

function addArtistResult(result, lastEntry) {
	var a = constructArtistDiv(result, lastEntry);
	$("#injectedResults").append(a);				
	++numResultsShowing;				
}

function addReleaseResult(result, lastEntry) {
	var a = constructReleaseDiv(result, lastEntry);
	$("#injectedResults").append(a);				
	++numResultsShowing;				
}			

function addLabelResult(result, lastEntry) {
	var a = constructLabelDiv(result, lastEntry);
	$("#injectedResults").append(a);				
	++numResultsShowing;				
}			

function constructVideoDiv(result, lastEntry) {				
	var posterUrl = 'image?filename=' + result.image + '&height=90&width=90';
	videoData[result.id] = result;		
	var videoElement = $('<img></img>');
	videoElement.attr('id', result.id + "_img");
	videoElement.attr('width', '90');
	videoElement.attr('height', '90');
	videoElement.attr('src', posterUrl);
	var durationDiv = $('<div></div>').addClass('duration').text(result.duration);
	var currentPositionDiv = $('<div></div>').addClass('position').attr('id', 'position_' + result.id).html(result.bpm + " " + result.key);				
	var divImage = $('<div></div>').addClass('resultEntryImage').attr('id', 'vid_container_' + result.id);				
	var divVideo = null;
	if (numResultsShowing == 0) {
		divVideo = $('<div></div>').addClass('resultEntry onlyResultEntry').attr('id', 'video_' + result.id);
		lastAddedResultDiv = 'video_' + result.id;		
	} else {
		if (lastEntry) {
			divVideo = $('<div></div>').addClass('resultEntry lastResultEntry').attr('id', 'video_' + result.id);
			if (lastAddedResultDiv != null) {
				if (numResultsShowing == 1)
					$("#" + lastAddedResultDiv).removeClass('resultEntry onlyResultEntry').addClass('resultEntry firstResultEntry');
				else
					$("#" + lastAddedResultDiv).removeClass('resultEntry lastResultEntry').addClass('resultEntry');
			}						
			lastAddedResultDiv = 'video_' + result.id;
		} else {
			divVideo = $('<div></div>').addClass('resultEntry').attr('id', 'video_' + result.id);
		}						
	}				
	var a = $('<a></a>').attr('id', 'a_' + result.id).attr('onclick', 'return playVideo(\'' + result.id + '\')').attr('href', 'javascript:playVideo(\'' + result.id + '\');').attr('class', 'resultLink');	
	a.append(divVideo);
	divVideo.append(divImage);
	var divDetailsContainer = $('<div></div>').addClass('resultEntryDetailsContainer');
	var divDetails = createDetailsDiv(result);
	divDetailsContainer.append(divDetails);
	divVideo.append(divDetailsContainer);	
				
	divImage.append(videoElement);
	if (((result.key != null) && (result.key.length > 0)) || ((result.bpm != null) && (result.bpm.length > 0)))
		divImage.append(currentPositionDiv);
	if ((result.duration != null) && (result.duration.length > 0))
		divImage.append(durationDiv);
	return a;									
}

function createDetailsDiv(result) {
	var divDetails = $('<div></div>').addClass('resultEntryDetails');
	var divContent = $('<div></div>').addClass('resultEntryContent');
	var divTitle = $('<div></div>').addClass('resultEntryTitle');
	if (result.track.length > 0)
		divTitle.text(result.track + ". " + result.title);
	else {
		divTitle.text(result.title);
	}
	var divArtistLine = $('<div></div>').addClass('resultEntryArtistLine');
	var divArtist = $('<div></div>').addClass('resultEntryArtist').html('by <span style="color:#FFFFFF">' + result.artists + '</span>');				
	var divNew = $('<div></div>').addClass('newVideo').text('New');
	var fromText = result.labels;
	var divLabel = $('<div></div>').addClass('resultEntryLabel').html('from <span style="color:#FFFFFF">' + fromText + '</span>');
	var releaseText = result.release_title;
	if ((result.year != null) && (result.year.length > 0)) {
		releaseText = releaseText + " [" + result.year + "]";
	}				
	var divRelease = $('<div></div>').addClass('resultEntryLabel').html('on <span style="color:#FFFFFF">' + releaseText + '</span>');
	divDetails.append(divContent);
	divArtistLine.append(divArtist);
	divContent.append(divTitle);
	if ((result.artists != null) && (result.artists.length > 0))
		divContent.append(divArtistLine);
	if ((releaseText != null) && (releaseText.length > 0))
		divContent.append(divRelease);
	if ((fromText != null) && (fromText.length > 0))
		divContent.append(divLabel);				
	return divDetails;
}


function constructArtistDiv(result, lastEntry) {				
	var posterUrl = 'image?filename=' + result.image + '&height=90&width=90';
	var videoElement = $('<img></img>');
	videoElement.attr('id', result.id + "_artist_img");
	videoElement.attr('width', '90');
	videoElement.attr('height', '90');
	videoElement.attr('src', posterUrl);
	var divImage = $('<div></div>').addClass('resultEntryImage').attr('id', 'artist_container_' + result.id);				
	var divVideo = null;
	if (numResultsShowing == 0) {
		divVideo = $('<div></div>').addClass('resultEntry onlyResultEntry').attr('id', 'artist_' + result.id);
		lastAddedResultDiv = 'artist_' + result.id;		
	} else {
		if (lastEntry) {
			divVideo = $('<div></div>').addClass('resultEntry lastResultEntry').attr('id', 'artist_' + result.id);
			if (lastAddedResultDiv != null) {
				if (numResultsShowing == 1)
					$("#" + lastAddedResultDiv).removeClass('resultEntry onlyResultEntry').addClass('resultEntry firstResultEntry');
				else
					$("#" + lastAddedResultDiv).removeClass('resultEntry lastResultEntry').addClass('resultEntry');
			}						
			lastAddedResultDiv = 'artist_' + result.id;
		} else {
			divVideo = $('<div></div>').addClass('resultEntry').attr('id', 'artist_' + result.id);
		}						
	}				
	var a = $('<a></a>').attr('id', 'a_artist_' + result.id).attr('onclick', 'return playArtist(\'' + result.id + '\')').attr('href', 'javascript:playArtist(\'' + result.id + '\');').attr('class', 'resultLink');	
	a.append(divVideo);
	divVideo.append(divImage);
	var divDetailsContainer = $('<div></div>').addClass('resultEntryDetailsContainer');
	var divDetails = createArtistDetailsDiv(result);
	divDetailsContainer.append(divDetails);
	divVideo.append(divDetailsContainer);	
				
	divImage.append(videoElement);
	return a;									
}

function createArtistDetailsDiv(result) {
	var divDetails = $('<div></div>').addClass('resultEntryDetails');
	var divContent = $('<div></div>').addClass('resultEntryContent');
	var divTitle = $('<div></div>').addClass('resultEntryTitle').text(result.artist);
	var divStyles = $('<div></div>').addClass('resultEntryFilters').html('styles <span style="color:#FFFFFF">' + result.styles + '</span>');
	var divTags = $('<div></div>').addClass('resultEntryFilters').html('tags <span style="color:#FFFFFF">' + result.tags + '</span>');
	divDetails.append(divContent);
	divContent.append(divTitle);
	if ((result.styles != null) && (result.styles.length > 0))
		divContent.append(divStyles);
	if ((result.tags != null) && (result.tags.length > 0))
		divContent.append(divTags);
	return divDetails;
}

function constructLabelDiv(result, lastEntry) {				
	var posterUrl = 'image?filename=' + result.image + '&height=90&width=90';
	var videoElement = $('<img></img>');
	videoElement.attr('id', result.id + "_label_img");
	videoElement.attr('width', '90');
	videoElement.attr('height', '90');
	videoElement.attr('src', posterUrl);
	var divImage = $('<div></div>').addClass('resultEntryImage').attr('id', 'label_container_' + result.id);				
	var divVideo = null;
	if (numResultsShowing == 0) {
		divVideo = $('<div></div>').addClass('resultEntry onlyResultEntry').attr('id', 'label_' + result.id);
		lastAddedResultDiv = 'label_' + result.id;		
	} else {
		if (lastEntry) {
			divVideo = $('<div></div>').addClass('resultEntry lastResultEntry').attr('id', 'label_' + result.id);
			if (lastAddedResultDiv != null) {
				if (numResultsShowing == 1)
					$("#" + lastAddedResultDiv).removeClass('resultEntry onlyResultEntry').addClass('resultEntry firstResultEntry');
				else
					$("#" + lastAddedResultDiv).removeClass('resultEntry lastResultEntry').addClass('resultEntry');
			}						
			lastAddedResultDiv = 'label_' + result.id;
		} else {
			divVideo = $('<div></div>').addClass('resultEntry').attr('id', 'label_' + result.id);
		}						
	}				
	var a = $('<a></a>').attr('id', 'a_label_' + result.id).attr('onclick', 'return playLabel(\'' + result.id + '\')').attr('href', 'javascript:playLabel(\'' + result.id + '\');').attr('class', 'resultLink');	
	a.append(divVideo);
	divVideo.append(divImage);
	var divDetailsContainer = $('<div></div>').addClass('resultEntryDetailsContainer');
	var divDetails = createLabelDetailsDiv(result);
	divDetailsContainer.append(divDetails);
	divVideo.append(divDetailsContainer);	
				
	divImage.append(videoElement);
	return a;									
}

function createLabelDetailsDiv(result) {
	var divDetails = $('<div></div>').addClass('resultEntryDetails');
	var divContent = $('<div></div>').addClass('resultEntryContent');
	var divTitle = $('<div></div>').addClass('resultEntryTitle').text(result.label);
	var divStyles = $('<div></div>').addClass('resultEntryFilters').html('styles <span style="color:#FFFFFF">' + result.styles + '</span>');
	var divTags = $('<div></div>').addClass('resultEntryFilters').html('tags <span style="color:#FFFFFF">' + result.tags + '</span>');
	divDetails.append(divContent);
	divContent.append(divTitle);
	if ((result.styles != null) && (result.styles.length > 0))
		divContent.append(divStyles);
	if ((result.tags != null) && (result.tags.length > 0))
		divContent.append(divTags);				
	return divDetails;
}		

function constructReleaseDiv(result, lastEntry) {				
	var posterUrl = 'image?filename=' + result.image + '&height=90&width=90';
	var videoElement = $('<img></img>');
	videoElement.attr('id', result.id + "_release_img");
	videoElement.attr('width', '90');
	videoElement.attr('height', '90');
	videoElement.attr('src', posterUrl);
	var divImage = $('<div></div>').addClass('resultEntryImage').attr('id', 'release_container_' + result.id);				
	var divVideo = null;
	if (numResultsShowing == 0) {
		divVideo = $('<div></div>').addClass('resultEntry onlyResultEntry').attr('id', 'release_' + result.id);
		lastAddedResultDiv = 'release_' + result.id;		
	} else {
		if (lastEntry) {
			divVideo = $('<div></div>').addClass('resultEntry lastResultEntry').attr('id', 'release_' + result.id);
			if (lastAddedResultDiv != null) {
				if (numResultsShowing == 1)
					$("#" + lastAddedResultDiv).removeClass('resultEntry onlyResultEntry').addClass('resultEntry firstResultEntry');
				else
					$("#" + lastAddedResultDiv).removeClass('resultEntry lastResultEntry').addClass('resultEntry');
			}						
			lastAddedResultDiv = 'release_' + result.id;
		} else {
			divVideo = $('<div></div>').addClass('resultEntry').attr('id', 'release_' + result.id);
		}						
	}				
	var a = $('<a></a>').attr('id', 'a_release_' + result.id).attr('onclick', 'return playRelease(\'' + result.id + '\')').attr('href', 'javascript:playRelease(\'' + result.id + '\');').attr('class', 'resultLink');	
	a.append(divVideo);
	divVideo.append(divImage);
	var divDetailsContainer = $('<div></div>').addClass('resultEntryDetailsContainer');
	var divDetails = createReleaseDetailsDiv(result);
	divDetailsContainer.append(divDetails);
	divVideo.append(divDetailsContainer);	
				
	divImage.append(videoElement);
	return a;									
}

function createReleaseDetailsDiv(result) {
	var divDetails = $('<div></div>').addClass('resultEntryDetails');
	var divContent = $('<div></div>').addClass('resultEntryContent');
	var titleText = result.release_title;
	if ((result.year != null) && (result.year.length > 0))
		titleText += " [" + result.year + "]";
	var divTitle = $('<div></div>').addClass('resultEntryTitle').text(titleText);
	var divArtistLine = $('<div></div>').addClass('resultEntryArtistLine');
	var divArtist = $('<div></div>').addClass('resultEntryArtist').html('by <span style="color:#FFFFFF">' + result.artists + '</span>');				
	var fromText = result.labels;
	var divLabel = $('<div></div>').addClass('resultEntryLabel').html('from <span style="color:#FFFFFF">' + fromText + '</span>');
	var divStyles = $('<div></div>').addClass('resultEntryFilters').html('styles <span style="color:#FFFFFF">' + result.styles + '</span>');
	var divTags = $('<div></div>').addClass('resultEntryFilters').html('tags <span style="color:#FFFFFF">' + result.tags + '</span>');
	divDetails.append(divContent);
	divContent.append(divTitle);
	divArtistLine.append(divArtist);
	if ((result.artists != null) && (result.artists.length > 0))
		divContent.append(divArtistLine);
	if ((fromText != null) && (fromText.length > 0))
		divContent.append(divLabel);
	if ((result.styles != null) && (result.styles.length > 0))
		divContent.append(divStyles);
	if ((result.tags != null) && (result.tags.length > 0))
		divContent.append(divTags);				
	return divDetails;
}	
	
function loadSpecificVideo(result) {
	document.getElementById("showMoreLink").style.display = 'none';
	document.getElementById("loadingText").style.display = 'none';	
	maxResultsReached = true;
	addVideoResult(result, true);
}

function advanceSong(currentId) {
	var queryParameters = { };
	$.getJSON(  
		"forward",  
		queryParameters,  
		function(results) {
			//alert('advancingSong(): results=' + results);
			for (key in results) {
				// add the first result
				var result = results[key];
				if (videoData[currentId].is_video) {
					$("#" + currentId).remove();
					var posterUrl = 'image?filename=' + videoData[currentId].image + '&height=90&width=90';
					var videoElement = $('<img></img>');
					videoElement.attr('id', videoData[currentId].id + "_img");
					videoElement.attr('width', '90');
					videoElement.attr('height', '90');
					videoElement.attr('src', posterUrl);
					$("#vid_container_" + currentId).append(videoElement);
				}							
				if (!document.getElementById(result.id)) {
					if (!$("#a_" + result.id).length) {
						var a = constructVideoDiv(result, (lastAddedResultDiv == ('video_' + currentId)));
						$("#a_" + currentId).after(a);
						++numResultsShowing;
					}								
					playVideoDirect(result.id, false);

					$.scrollTo($('#a_' + result.id)); //, { offset: { top: -40, left: 0 } });
				} else {
					document.getElementById(result.id).load();
					document.getElementById(result.id).play();

					$.scrollTo($('#a_' + result.id)); //, { offset: { top: -40, left: 0 } });
				}
			}
		}  
	);				
}

function showMoreVideos() {
	return showMoreVideosDirect(false);
}

function showMoreVideosDirect(playFirstItem) {
	//alert('showing more videos');
	document.getElementById("noResultsText").style.display = 'none';
	document.getElementById("showMoreLink").style.display = 'none';
	document.getElementById("loadingText").style.display = 'block';	
	var queryParameters = { start: numResultsShowing, count: numResultsPerQuery, q: currentSearchQuery, playlist: currentView, artist_id: currentArtistId, label_id: currentLabelId, release_id: currentReleaseId };
	var url = "songs";
	if (currentType == 'type_artists')
		url = "artists";
	else if (currentType == 'type_labels')
		url = "labels";
	else if (currentType == 'type_releases')
		url = "releases";
	$.getJSON(  
		url,  
		queryParameters,  
		function(results) {
			if (numResultsShowing + results.items.length < results.numRows)
				maxResultsReached = false;
			else
				maxResultsReached = true;
			results = results.items;
			if (results.length > 0) {
				document.getElementById("loadingText").style.display = 'none';
			} else {
				if (numResultsShowing == 0) {
					document.getElementById("loadingText").style.display = 'none';	
					document.getElementById("noResultsText").style.display = 'block';
				}
			}
			var firstResult = null;
			for (key in results) {
				if (key < numResultsPerQuery) {
					var result = results[key];
					if (currentType == 'type_songs')
						addVideoResult(result, true);
					if (currentType == 'type_artists')
						addArtistResult(result, true);
					else if (currentType == 'type_labels')
						addLabelResult(result, true);
					else if (currentType == 'type_releases')
						addReleaseResult(result, true);
					if (firstResult == null)
						firstResult = result;										
				}
			}
			if (!maxResultsReached)
				document.getElementById("showMoreLink").style.display = 'block';
			if (playFirstItem && (firstResult != null)) {
				playVideoDirect(firstResult.id, false);
			}
		}  
	);	
}

function showSearchBar() {
	document.getElementById("searchBar").style.display = 'block';
	document.getElementById("searchLink").style.display = 'none';
	document.getElementById("cancelSearchLink").style.display = 'block';
}

function hideSearchBar() {
	document.getElementById("searchBar").style.display = 'none';
	document.getElementById("searchLink").style.display = 'block';
	document.getElementById("cancelSearchLink").style.display = 'none';
	currentSearchQuery = null;
}

function playArtist(artistId) {
	clearSearchParams();
	currentArtistId = artistId;
	selectTypeDirect('type_songs', false, true);				
}
function playLabel(labelId) {
	clearSearchParams();
	currentLabelId = labelId;
	selectTypeDirect('type_songs', false, true);
}
function playRelease(releaseId) {
	clearSearchParams();
	currentReleaseId = releaseId;
	selectTypeDirect('type_songs', false, true);
}

function playVideo(videoId) {
	return playVideoDirect(videoId, true);
}
// we only want to seed the song if it came from a user's direct selection (not autoplay)
function playVideoDirect(videoId, seed) {
	if (!document.getElementById(videoId)) {
		if (document.getElementById('ratingControl'))					
			$('#ratingControl').remove();					
		if (document.getElementById('videoControl'))						
			$('#videoControl').remove();						
		var result = videoData[videoId];
		var videoControlElement = $('<div></div>').attr('id', 'videoControl');				
		var videoElement = null;
		if (result.is_video)
			videoElement = $('<video></video>');
		else
			videoElement = $('<audio></audio>');
		var posterUrl = 'image?filename=' + result.image + '&height=90&width=90';
		if (seed) {
			$.get(
					"seed",  
					{id: videoId},  
					function(responseText){ },  
					"text"
				);								
		}
		var videoUrl =  result.src;
		videoElement.attr('id', result.id);
		if (!result.is_video)
			videoElement.attr('class', 'audioElement');
		videoElement.attr('controls', 'controls');
		videoElement.attr('loop', false);
		videoElement.attr('autoplay', 'autoplay');
		videoElement.attr('poster', posterUrl);
		videoElement.attr('src', videoUrl);
		videoElement.attr('width', '90');
		videoElement.attr('height', '90');
		videoElement.attr('onclick', 'this.play();');
		videoElement.text('HTML5 audio is not supported on your browser.');
						
		var horizontalDiv = $('<div></div>').attr('id', 'ratingControl');
							
		var nextLink = $('<a></a>');
		nextLink.attr('class', 'nextElement');
		nextLink.attr('href', 'javascript:advanceSong(\'' + result.id + '\');');
		//nextLink.text('NEXT');					
		
		var ratingDiv = $('<div></div>').attr('id', 'rating_' + result.id);
		if (result.is_video)
			ratingDiv.addClass('ratingElementVideo');
		else
			ratingDiv.addClass('ratingElement');
		var rating1Star = $('<input></input>').attr('type', 'radio').attr('name', 'rateSong_' + result.id).addClass('star').attr('value', '1');
		var rating2Star = $('<input></input>').attr('type', 'radio').attr('name', 'rateSong_' + result.id).addClass('star').attr('value', '2');
		var rating3Star = $('<input></input>').attr('type', 'radio').attr('name', 'rateSong_' + result.id).addClass('star').attr('value', '3');
		var rating4Star = $('<input></input>').attr('type', 'radio').attr('name', 'rateSong_' + result.id).addClass('star').attr('value', '4');
		var rating5Star = $('<input></input>').attr('type', 'radio').attr('name', 'rateSong_' + result.id).addClass('star').attr('value', '5');
		if (result.rating == 1)
			rating1Star.attr('checked', 'checked');
		if (result.rating == 2)
			rating2Star.attr('checked', 'checked');
		if (result.rating == 3)
			rating3Star.attr('checked', 'checked');
		if (result.rating == 4)
			rating4Star.attr('checked', 'checked');
		if (result.rating == 5)
			rating5Star.attr('checked', 'checked');
		ratingDiv.append(rating1Star);
		ratingDiv.append(rating2Star);
		ratingDiv.append(rating3Star);
		ratingDiv.append(rating4Star);
		ratingDiv.append(rating5Star);

		horizontalDiv.append(ratingDiv);
		horizontalDiv.append(nextLink);
		
		videoControlElement.append(videoElement);
		videoControlElement.append(horizontalDiv);										

		if (result.is_video) {
			$('#' + result.id + "_img").remove();		
			$('#vid_container_' + result.id).append(videoElement);	
			horizontalDiv.addClass('ratingNextBarVideo');
			//$('#video_' + result.id).parent().prepend(horizontalDiv);			
		} else {
			horizontalDiv.addClass('ratingNextBar')
			$('#video_' + result.id).parent().prepend(videoControlElement);	
		}
		$('input.star').rating({
			callback: function(value, link) {
				var ratingValue = 0;
				if (value)
					ratingValue = value;
				$.get(
					"rate_song",  
					{id: result.id, value: ratingValue},  
					function(responseText){ },  
					"text"
				);		
				videoData[result.id].rating = ratingValue;
			}
		});
							
		document.getElementById(videoId).load();
		document.getElementById(videoId).play();
		
		$('#' + result.id).bind("ended", function(e){
			//alert('ended');						
			advanceSong(result.id);
			numErrors = 0;						
		});				
		$('#' + result.id).bind("error", function(e){						
			//alert('error');
			++numErrors;
			if (numErrors >= maxErrors) {
				//alert('max autoplay errors reached');
			} else {
				advanceSong(result.id);
			}
		});				
	} else {		
		if (document.getElementById(videoId).paused)
			document.getElementById(videoId).load();
		document.getElementById(videoId).play();
	}
	return false;
}

function clearSearchParams() {
	currentSearchQuery = null;
	currentArtistId = null;
	currentLabelId = null;
	currentReleaseId = null;
}

function fetchResults() {
	return fetchResultsDirect(false);
}
function fetchResultsDirect(playFirstResult) {
	$('.resultLink').remove();
	numResultsShowing = 0;
	lastAddedResultDiv = null;
	showMoreVideosDirect(playFirstResult);
}

function search() {
	clearSearchParams();
	currentSearchQuery = document.getElementById("query").value;
	//alert('searching=' + currentSearchQuery);
	if ((currentView == null) && (defaultView != null)) {
		currentView = defaultView;
		$("#" + currentView).addClass('viewLinkSelected');
	}					
	fetchResults();
}

function clearSearch() {
	clearSearchParams();
	document.getElementById("query").value = "";
	updateClearButton();
	fetchResults();
}

function updateClearButton() {	
	currentSearchQuery = document.getElementById("query").value;
	if (currentSearchQuery.length > 0) {
		$("#clearSearchLink").removeClass('cancelSearchLinkHidden').addClass('cancelSearchLinkVisible');
		$("#searchContainer").removeClass('searchContainerCleared').addClass('searchContainerWithText');
	} else {
		$("#clearSearchLink").removeClass('cancelSearchLinkVisible').addClass('cancelSearchLinkHidden');
		$("#searchContainer").removeClass('searchContainerWithText').addClass('searchContainerCleared');
	}
}

function selectView(link) {
	currentArtistId = null;
	currentLabelId = null;
	currentReleaseId = null;			
	if (currentView != null)
		$("#" + currentView).removeClass('viewLinkSelected').addClass('viewLink');
	$("#" + link).addClass('viewLinkSelected');				
	currentView = link;
	fetchResults();
}

function selectType(type) {
	return selectTypeDirect(type, true, false);
}
function selectTypeDirect(type, setView, playFirstResult) {
	if (currentType != null)
		$("#" + currentType).removeClass('typeSelectedItem').addClass('typeItem');
	$("#" + type).addClass('typeSelectedItem');
	currentType = type;
	currentSearchQuery = null;
	if (setView && (currentView == null) && (defaultView != null)) {
		currentView = defaultView;
		$("#" + currentView).addClass('viewLinkSelected');
	}				
	fetchResultsDirect(playFirstResult);
}		