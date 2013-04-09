function gmapcenter_as_array(gmap) {
    gmapcenter = map.getCenter();
    y = ''+gmapcenter;
    y = y.replace(/[^\d,-\.]/g, '');
    return y.split(',');
}

function coordfrac2nautical(v,what) {

    if(what=='lat') {
        direction = (v>0) ? 'N' : 'S';
    } else {
        direction = (v>0) ? 'E' : 'W';
    }
    v = Math.abs(v);
    
    tmp = Math.floor(v);
    y = tmp + '°';
    
    v = (v-tmp)*60;
    tmp = Math.floor(v);
    y += ' ' + tmp + "'";
    
    v = (v-tmp)*60;
    y += ' ' + Math.round(v) + '"';
    
    return y + ' ' + direction;
    
}

function setcoord(coord, v) {
    $('#'+coord).val(v);
    $('#g_'+coord).html(coordfrac2nautical(v, coord));
}

function gmapcenter2form(map) {
    coords = gmapcenter_as_array(map);
    setcoord('lat',coords[0]);
    setcoord('lon',coords[1]);
    return false;
}

// js from http://tech.cibul.net/geocode-with-google-maps-api-v3/
// map from http://www.daftlogic.com/sandbox-google-maps-centre-crosshairs.htm

//Useful links:
// http://code.google.com/apis/maps/documentation/javascript/reference.html#Marker
// http://code.google.com/apis/maps/documentation/javascript/services.html#Geocoding
// http://jqueryui.com/demos/autocomplete/#remote-with-cache
      
var geocoder;
var map;
var marker;
    
function initializeMap(){

    
    
//MAP
    var crosshairShape = {coords:[0,0,0,0],type:'rect'};
    var latlng = new google.maps.LatLng($('#lat').val(),$('#lon').val());
    var myOptions = {
        zoom:12,
        center:latlng,
        mapTypeId:google.maps.MapTypeId.ROADMAP ,
        draggableCursor:'crosshair',
        mapTypeControlOptions:{style:google.maps.MapTypeControlStyle.DROPDOWN_MENU}};
    map = new google.maps.Map(document.getElementById("map_canvas"),myOptions);
    marker = new google.maps.Marker({
        map: map,
        shape: crosshairShape
    });
    marker.bindTo('position', map, 'center'); 
  
  //GEOCODER
  geocoder = new google.maps.Geocoder();
  
  gmapcenter2form(map);
  
  //$('#map_picker').hide();
  
  /*
  $('#date').datepicker({ 
    dateFormat: 'yy-mm-dd',
    firstDay: 1
  });
  */
}

function populateTz(xml) { populateSelect(xml, "tz", "z", "Europe/Tallinn"); }
function populateF(xml) { populateSelect(xml, "f", "f", ""); }

function populateSelect(xml, name, valueTag, defaultValue) {
  var select = document.getElementById(name);
  $(xml).find(valueTag).each(function()
  {
    var el = document.createElement("option");
    el.textContent = $(this).text();
    el.value = $(this).text();
    if($(this).text() == defaultValue) {
        el.selected = true;
    }
    select.appendChild(el);
  });
}

function URL2Map () {
	y = {};
	query = location.search.substring(1);
	pairs = query.split('&');
	for(i=0; i<pairs.length; i++) {
		parameter = pairs[i].split('=');
		for(j=0; j<parameter.length; j++) {
			parameter[j] = decodeURIComponent(parameter[j]);
		}
		key = parameter.shift();
		y[key] = parameter.join('=');
	}
	return y;
}

$(document).ready(function() { 
         
  request = URL2Map ();
  if('jsOnload' in request) {
	  if(request['jsOnload'] == 'initMV') { initMV(); }
  }
  
  initializeMap();
  
	$.ajax({
		type: "GET",
		url: "ajaxdata?type=available_timezones&onlyplaces=1&sort=1",
		dataType: "xml",
		success: populateTz
	});
	
	$.ajax({
		type: "GET",
		url: "ajaxdata?type=supported_formats",
		dataType: "xml",
		success: populateF
	});
                  
  $(function() {
    $("#address").autocomplete({
      //This bit uses the geocoder to fetch address values
      source: function(request, response) {
        geocoder.geocode( {'address': request.term }, function(results, status) {
          response($.map(results, function(item) {
            return {
              label:  item.formatted_address,
              value: item.formatted_address,
              latitude: item.geometry.location.lat(),
              longitude: item.geometry.location.lng()
            }
          }));
        })
      },
      //This bit is executed upon selection of an address
      select: function(event, ui) {
        $("#lat").val(ui.item.latitude);
        $("#lon").val(ui.item.longitude);
        var location = new google.maps.LatLng(ui.item.latitude, ui.item.longitude);
        marker.setPosition(location);
        map.setCenter(location);
        gmapcenter2form(map);
      }
    });
  });
  
});

function initMV() {
  document.forms['input_data'].elements['ad'].checked = false;
  document.forms['input_data'].elements['mo'].checked = true;
  document.forms['input_data'].elements['sl'].checked = true;
  document.forms['input_data'].elements['ge'].checked = true;
  for(var i = 0; i < document.forms['input_data'].elements['ce'].length; i++) {
    if(document.forms['input_data'].elements['ce'][i].value == 'm') {
      document.forms['input_data'].elements['ce'][i].checked = true;
      break;
    }
  }
  return false;
}